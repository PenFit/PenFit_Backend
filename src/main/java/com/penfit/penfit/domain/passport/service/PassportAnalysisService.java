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

import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportAnalysisService {

    private static final int REQUIRED_ANALYSIS_COUNT = 6;

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
            validate(response, context);
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

    private void validate(PassportAnalyzeResponse response, AnalysisContext context) {
        require(response.detailedAnalysis() != null
                        && response.detailedAnalysis().size() == REQUIRED_ANALYSIS_COUNT,
                "detailedAnalysis 는 %d개여야 한다".formatted(REQUIRED_ANALYSIS_COUNT));
        require(response.sustainableMonthlyContribution() != null
                        && response.sustainableMonthlyContribution() >= 0,
                "sustainableMonthlyContribution 은 0 이상이어야 한다");
        require(hasText(response.analysisSummary()), "analysisSummary 가 비어 있다");
        require(hasText(response.judgmentReason()), "judgmentReason 이 비어 있다");
        require(hasText(response.modelVersion()), "modelVersion 이 비어 있다");
        require(isEnum(PassportTypeCode.class, response.typeCode()),
                "알 수 없는 typeCode: " + response.typeCode());
        require(response.marketRiskLevel() != null
                        && isEnum(MarketRiskLevel.class, response.marketRiskLevel().code()),
                "알 수 없는 marketRiskLevel");
        require(response.biggestInterruptionRisk() != null
                        && isEnum(ScenarioCode.class, response.biggestInterruptionRisk().scenarioCode()),
                "알 수 없는 biggestInterruptionRisk");

        Set<ScenarioCode> seen = EnumSet.noneOf(ScenarioCode.class);
        for (PassportAnalyzeResponse.DetailedAnalysis analysis : response.detailedAnalysis()) {
            require(isEnum(ScenarioCode.class, analysis.scenarioCode()),
                    "알 수 없는 scenarioCode: " + analysis.scenarioCode());
            ScenarioCode scenarioCode = ScenarioCode.valueOf(analysis.scenarioCode());
            require(seen.add(scenarioCode), "scenarioCode 가 중복됐다: " + scenarioCode);
            require(hasText(analysis.behaviorSummary()), "behaviorSummary 가 비어 있다: " + scenarioCode);
            require(hasText(analysis.interpretation()), "interpretation 이 비어 있다: " + scenarioCode);
            require(context.answers().containsKey(scenarioCode),
                    "사용자가 답변하지 않은 상황이다: " + scenarioCode);
            require(context.answers().get(scenarioCode).name().equals(analysis.selectedOptionCode()),
                    "선택지가 저장된 답변과 다르다: %s 저장 %s, 응답 %s".formatted(
                            scenarioCode, context.answers().get(scenarioCode), analysis.selectedOptionCode()));
        }
        require(seen.size() == REQUIRED_ANALYSIS_COUNT,
                "6개 상황이 모두 있어야 한다");
    }

    private void require(boolean condition, String reason) {
        if (!condition) {
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE, reason);
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
