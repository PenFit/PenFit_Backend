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
            T response = clients.get(api).post()
                    .uri(api.getPath())
                    .header(API_KEY_HEADER, properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw toServerException(res);
                    })
                    .body(responseType);

            long duration = System.currentTimeMillis() - startedAt;
            if (response == null) {
                aiCallLogger.failure(userId, api, request, 200, null, duration);
                throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
            }
            aiCallLogger.success(userId, api, request, response, duration);
            return response;

        } catch (AiServerException e) {
            aiCallLogger.failure(userId, api, request, e.getHttpStatus(),
                    e.getAiErrorCode(), System.currentTimeMillis() - startedAt);
            log.warn("AI 호출 실패 api={} status={} code={}", api, e.getHttpStatus(), e.getAiErrorCode());
            throw new BusinessException(e.getErrorCode());

        } catch (ResourceAccessException e) {
            boolean timedOut = e.getCause() instanceof SocketTimeoutException;
            ErrorCode errorCode = timedOut ? ErrorCode.AI_TIMEOUT : ErrorCode.AI_SERVER_ERROR;

            aiCallLogger.failure(userId, api, request, null, timedOut ? "TIMEOUT" : "CONNECT_FAILED",
                    System.currentTimeMillis() - startedAt);
            log.error("AI 서버 통신 실패 api={} timedOut={}", api, timedOut, e);
            throw new BusinessException(errorCode, e);
        }
    }

    private AiServerException toServerException(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        AiErrorResponse body = readError(response);
        String code = body == null ? null : body.code();
        return new AiServerException(status, code, toErrorCode(status, code),
                body == null ? null : body.message());
    }

    private AiErrorResponse readError(ClientHttpResponse response) {
        try {
            byte[] bytes = response.getBody().readAllBytes();
            if (bytes.length == 0) {
                return null;
            }
            return objectMapper.readValue(bytes, AiErrorResponse.class);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private ErrorCode toErrorCode(int status, String aiErrorCode) {
        if (status == 504) {
            return ErrorCode.AI_TIMEOUT;
        }
        if (status == 422) {
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
