package com.penfit.penfit.domain.passport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.passport.service.PassportAnalysisStore.AnalysisContext;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeResponse;
import com.penfit.penfit.global.enums.MarketRiskLevel;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class PassportAnalysisService {

    private final AiClient aiClient;
    private final PassportAnalysisStore passportAnalysisStore;
    private final ObjectMapper objectMapper;

    public void analyze(Long rehearsalId) {
        AnalysisContext context = passportAnalysisStore.loadContext(rehearsalId);
        if (context == null) {
            return;
        }

        try {
            PassportAnalyzeResponse response = aiClient.call(
                    AiApi.PASSPORT, context.request(), PassportAnalyzeResponse.class, context.userId());
            validate(response);
            passportAnalysisStore.savePassport(context, response, toJson(response));

        } catch (BusinessException e) {
            log.warn("연금 패스포트 분석 실패 rehearsalId={} code={} reason={}",
                    rehearsalId, e.getErrorCode().getCode(), e.getMessage());
            passportAnalysisStore.markFailed(rehearsalId, e.getErrorCode().getCode(), e.getErrorCode().getMessage());

        } catch (RuntimeException e) {
            log.error("연금 패스포트 분석 중 예상하지 못한 오류 rehearsalId={}", rehearsalId, e);
            passportAnalysisStore.markFailed(rehearsalId,
                    ErrorCode.AI_SERVER_ERROR.getCode(), ErrorCode.AI_SERVER_ERROR.getMessage());
        }
    }

    public void markFailed(Long rehearsalId, ErrorCode errorCode, String message) {
        passportAnalysisStore.markFailed(rehearsalId, errorCode.getCode(), message);
    }

    private void validate(PassportAnalyzeResponse response) {
        require(response.sustainableMonthlyContribution() != null
                        && response.sustainableMonthlyContribution() >= 0,
                "sustainableMonthlyContribution 은 0 이상이어야 한다");
        require(hasText(response.analysisSummary()), "analysisSummary 가 비어 있다");
        require(hasText(response.judgmentReason()), "judgmentReason 이 비어 있다");
        require(hasText(response.detailedAnalysisReport()), "detailedAnalysisReport 가 비어 있다");
        require(hasText(response.modelVersion()), "modelVersion 이 비어 있다");
        require(isEnum(PassportTypeCode.class, response.typeCode()),
                "알 수 없는 typeCode: " + response.typeCode());
        require(response.marketRiskLevel() != null
                        && isEnum(MarketRiskLevel.class, response.marketRiskLevel().code()),
                "알 수 없는 marketRiskLevel");
        require(response.biggestInterruptionRisk() != null
                        && isEnum(ScenarioCode.class, response.biggestInterruptionRisk().scenarioCode()),
                "알 수 없는 biggestInterruptionRisk");
    }

    private void require(boolean condition, String reason) {
        if (!condition) {
            log.warn("연금 패스포트 응답 검증 실패 reason={}", reason);
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <E extends Enum<E>> boolean isEnum(Class<E> type, String value) {
        if (value == null) {
            return false;
        }
        try {
            Enum.valueOf(type, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String toJson(PassportAnalyzeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE, e);
        }
    }
}
