package com.penfit.penfit.global.client.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.client.ai.dto.AiErrorResponse;
import com.penfit.penfit.global.config.AiProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class AiClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String INSUFFICIENT_RECOMMENDATIONS = "INSUFFICIENT_RECOMMENDATIONS";
    private static final String NO_ACTIONABLE_SPENDING = "NO_ACTIONABLE_SPENDING";
    private static final String RATE_LIMIT_MARKER = "tokens per minute";
    private static final String DAILY_LIMIT_MARKER = "tokens per day";

    private final Map<AiApi, RestClient> clients = new EnumMap<>(AiApi.class);
    private final AiProperties properties;
    private final AiCallLogger aiCallLogger;
    private final ObjectMapper objectMapper;

    public AiClient(AiProperties properties, AiCallLogger aiCallLogger, ObjectMapper objectMapper) {
        this.properties = properties;
        this.aiCallLogger = aiCallLogger;
        this.objectMapper = objectMapper;

        clients.put(AiApi.PASSPORT, build(properties.timeout().passport()));
        clients.put(AiApi.PENSION_PLAN, build(properties.timeout().pensionPlan()));
        clients.put(AiApi.PRODUCT_RECOMMENDATION, build(properties.timeout().productRecommendation()));
        clients.put(AiApi.SPENDING_MISSION, build(properties.timeout().spendingMission()));
    }

    public <T> T call(AiApi api, Object request, Class<T> responseType, Long userId) {
        long startedAt = System.currentTimeMillis();
        try {
            String body = clients.get(api).post()
                    .uri(api.getPath())
                    .header(API_KEY_HEADER, properties.apiKey())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(writeBody(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw toServerException(res);
                    })
                    .body(String.class);

            long duration = System.currentTimeMillis() - startedAt;
            T response = readBody(body, responseType);
            if (response == null) {
                aiCallLogger.failure(userId, api, request, 200, null, duration);
                throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
            }
            aiCallLogger.success(userId, api, request, response, duration);
            return response;

        } catch (AiServerException e) {
            aiCallLogger.failure(userId, api, request, e.getHttpStatus(),
                    e.getAiErrorCode(), System.currentTimeMillis() - startedAt, e.getResponseBody());
            log.warn("AI 호출 실패 api={} status={} code={}", api, e.getHttpStatus(), e.getAiErrorCode());
            throw new BusinessException(e.getErrorCode());

        } catch (BusinessException e) {
            throw e;

        } catch (ResourceAccessException e) {
            boolean timedOut = e.getCause() instanceof SocketTimeoutException;
            ErrorCode errorCode = timedOut ? ErrorCode.AI_TIMEOUT : ErrorCode.AI_SERVER_ERROR;

            aiCallLogger.failure(userId, api, request, null, timedOut ? "TIMEOUT" : "CONNECT_FAILED",
                    System.currentTimeMillis() - startedAt);
            log.error("AI 서버 통신 실패 api={} timedOut={}", api, timedOut, e);
            throw new BusinessException(errorCode, e);

        } catch (RuntimeException e) {
            boolean timedOut = hasTimeout(e);
            aiCallLogger.failure(userId, api, request, null, timedOut ? "TIMEOUT" : "RESPONSE_ERROR",
                    System.currentTimeMillis() - startedAt);
            log.error("AI 응답을 처리하지 못했습니다 api={} timedOut={}", api, timedOut, e);
            throw new BusinessException(
                    timedOut ? ErrorCode.AI_TIMEOUT : ErrorCode.AI_INVALID_RESPONSE, e);
        }
    }

    private boolean hasTimeout(Throwable throwable) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private String writeBody(Object request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (IOException e) {
            throw new IllegalStateException("AI 요청을 변환하지 못했습니다.", e);
        }
    }

    private <T> T readBody(String body, Class<T> responseType) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, responseType);
        } catch (IOException e) {
            throw new IllegalStateException("AI 응답을 변환하지 못했습니다.", e);
        }
    }

    private AiServerException toServerException(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        String raw = readRawBody(response);
        AiErrorResponse body = readError(raw);
        String code = body == null ? null : body.code();
        return new AiServerException(status, code, toErrorCode(status, code, raw),
                body == null ? null : body.message(), raw);
    }

    private String readRawBody(ClientHttpResponse response) {
        try {
            byte[] bytes = response.getBody().readAllBytes();
            return bytes.length == 0 ? null : new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private AiErrorResponse readError(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, AiErrorResponse.class);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    static ErrorCode toErrorCode(int status, String aiErrorCode, String raw) {
        if (raw != null) {
            if (raw.contains(DAILY_LIMIT_MARKER)) {
                return ErrorCode.AI_DAILY_LIMIT_EXCEEDED;
            }
            if (raw.contains(RATE_LIMIT_MARKER)) {
                return ErrorCode.AI_RATE_LIMITED;
            }
        }
        if (status == 504) {
            return ErrorCode.AI_TIMEOUT;
        }
        if (status == 422) {
            if (INSUFFICIENT_RECOMMENDATIONS.equals(aiErrorCode)) {
                return ErrorCode.INSUFFICIENT_RECOMMENDATIONS;
            }
            if (NO_ACTIONABLE_SPENDING.equals(aiErrorCode)) {
                return ErrorCode.NO_ACTIONABLE_SPENDING;
            }
            return ErrorCode.AI_ANALYSIS_FAILED;
        }
        if (status == 400) {
            return ErrorCode.AI_INVALID_RESPONSE;
        }
        return ErrorCode.AI_SERVER_ERROR;
    }

    private RestClient build(Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) readTimeout.toMillis());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
