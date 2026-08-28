package com.penfit.penfit.global.client.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.client.ai.entity.AiCallLog;
import com.penfit.penfit.global.client.ai.repository.AiCallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiCallLogger {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final String UNSERIALIZABLE = "{}";

    private final AiCallLogRepository aiCallLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(Long userId, AiApi api, Object request, Object response, long durationMs) {
        save(AiCallLog.builder()
                .userId(userId)
                .apiName(api.name())
                .requestPayload(toJson(request))
                .responsePayload(toJson(response))
                .httpStatus(200)
                .durationMs((int) durationMs)
                .status(SUCCESS)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failure(Long userId, AiApi api, Object request, Integer httpStatus,
                        String aiErrorCode, long durationMs) {
        save(AiCallLog.builder()
                .userId(userId)
                .apiName(api.name())
                .requestPayload(toJson(request))
                .httpStatus(httpStatus)
                .aiErrorCode(aiErrorCode)
                .durationMs((int) durationMs)
                .status(FAILED)
                .build());
    }

    private void save(AiCallLog callLog) {
        try {
            aiCallLogRepository.save(callLog);
        } catch (RuntimeException e) {
            log.error("AI 호출 이력 저장 실패 api={}", callLog.getApiName(), e);
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (RuntimeException | com.fasterxml.jackson.core.JsonProcessingException e) {
            log.warn("AI 호출 payload 직렬화 실패", e);
            return UNSERIALIZABLE;
        }
    }
}
