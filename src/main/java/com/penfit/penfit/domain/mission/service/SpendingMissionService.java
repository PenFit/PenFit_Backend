package com.penfit.penfit.domain.mission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.domain.mission.dto.BehaviorMissionResponse;
import com.penfit.penfit.domain.mission.dto.MissionCompletionResponse;
import com.penfit.penfit.domain.mission.dto.SpendingAnalysisResponse;
import com.penfit.penfit.domain.mission.service.SpendingMissionStore.AnalysisContext;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.SpendingMissionAnalyzeResponse;
import com.penfit.penfit.global.enums.CategoryCode;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingMissionService {

    private static final int REQUIRED_INSIGHT_COUNT = 3;
    private static final BigDecimal RATIO_TOLERANCE = new BigDecimal("0.05");

    private final AiClient aiClient;
    private final SpendingMissionStore spendingMissionStore;
    private final ObjectMapper objectMapper;

    public SpendingAnalysisResponse analyze(Long userId) {
        AnalysisContext context = spendingMissionStore.loadContext(userId);

        SpendingMissionAnalyzeResponse response = aiClient.call(
                AiApi.SPENDING_MISSION, context.request(), SpendingMissionAnalyzeResponse.class, userId);
        validate(response);

        return spendingMissionStore.save(context, response, toJson(response));
    }

    public SpendingAnalysisResponse getMyAnalysis(Long userId) {
        return spendingMissionStore.getMyAnalysis(userId);
    }

    public BehaviorMissionResponse getCurrentMission(Long userId) {
        return spendingMissionStore.getCurrentMission(userId);
    }

    public BehaviorMissionResponse start(Long userId, Long missionId) {
        return spendingMissionStore.start(userId, missionId);
    }

    public BehaviorMissionResponse complete(Long userId, Long missionId) {
        return spendingMissionStore.complete(userId, missionId);
    }

    public MissionCompletionResponse getCompletions(Long userId, int year) {
        return spendingMissionStore.getCompletions(userId, year);
    }

    private void validate(SpendingMissionAnalyzeResponse response) {
        require(hasText(response.modelVersion()), "modelVersion 이 비어 있다");

        SpendingMissionAnalyzeResponse.WeeklySpendingAnalysis analysis = response.weeklySpendingAnalysis();
        require(analysis != null, "weeklySpendingAnalysis 가 없다");
        require(isCategory(analysis.topCategory()), "알 수 없는 topCategory: " + analysis.topCategory());
        require(hasText(analysis.summary()), "summary 가 비어 있다");

        List<SpendingMissionAnalyzeResponse.CategorySpending> spending = analysis.categorySpending();
        require(spending != null && !spending.isEmpty(), "categorySpending 이 비어 있다");

        Set<CategoryCode> categories = new HashSet<>();
        BigDecimal ratioSum = BigDecimal.ZERO;
        for (SpendingMissionAnalyzeResponse.CategorySpending item : spending) {
            require(isCategory(item.categoryCode()), "알 수 없는 categoryCode: " + item.categoryCode());
            require(categories.add(CategoryCode.valueOf(item.categoryCode())),
                    "categoryCode 가 중복됐다: " + item.categoryCode());
            require(item.amount() != null && item.amount() >= 0, "지출 금액은 0 이상이어야 한다");
            require(item.ratio() != null
                            && item.ratio().compareTo(BigDecimal.ZERO) >= 0
                            && item.ratio().compareTo(BigDecimal.ONE) <= 0,
                    "ratio 가 0~1 범위를 벗어났다: " + item.ratio());
            ratioSum = ratioSum.add(item.ratio());
        }
        require(ratioSum.subtract(BigDecimal.ONE).abs().compareTo(RATIO_TOLERANCE) <= 0,
                "ratio 의 합이 1 에서 벗어났다: " + ratioSum);

        List<String> insights = analysis.insights();
        require(insights != null && insights.size() >= REQUIRED_INSIGHT_COUNT,
                "insights 는 %d개 이상이어야 한다".formatted(REQUIRED_INSIGHT_COUNT));
        require(insights.stream().allMatch(this::hasText), "insights 에 빈 문장이 있다");

        SpendingMissionAnalyzeResponse.WeeklyMission mission = response.weeklyMission();
        require(mission != null, "weeklyMission 이 없다");
        require(hasText(mission.title()), "미션 title 이 비어 있다");
        require(hasText(mission.description()), "미션 description 이 비어 있다");
        require(hasText(mission.reason()), "미션 reason 이 비어 있다");
        require(isCategory(mission.targetCategory()),
                "알 수 없는 targetCategory: " + mission.targetCategory());
        require(mission.targetAmount() != null && mission.targetAmount() >= 0,
                "targetAmount 는 0 이상이어야 한다");
    }

    private boolean isCategory(String value) {
        if (value == null) {
            return false;
        }
        try {
            CategoryCode.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void require(boolean condition, String reason) {
        if (!condition) {
            log.warn("소비 분석 응답 검증 실패 reason={}", reason);
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }
    }

    private String toJson(SpendingMissionAnalyzeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE, e);
        }
    }
}
