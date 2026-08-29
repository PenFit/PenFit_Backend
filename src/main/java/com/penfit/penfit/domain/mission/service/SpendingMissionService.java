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
    private static final long TARGET_AMOUNT_UNIT = 5_000L;
    private static final long TARGET_AMOUNT_MIN = 5_000L;
    private static final long TARGET_AMOUNT_MAX = 50_000L;
    private static final BigDecimal RATIO_TOTAL = BigDecimal.valueOf(100);
    private static final BigDecimal RATIO_TOLERANCE = BigDecimal.ONE;

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

        SpendingMissionAnalyzeResponse.SpendingAnalysisPayload analysis = response.spendingAnalysis();
        require(analysis != null, "spendingAnalysis 가 없다");
        require(isCategory(analysis.topCategoryCode()),
                "알 수 없는 topCategoryCode: " + analysis.topCategoryCode());
        require(analysis.recurringExpense() != null && analysis.recurringExpense() >= 0,
                "recurringExpense 는 0 이상이어야 한다");
        require(analysis.reducibleAmount() != null && analysis.reducibleAmount() >= 0,
                "reducibleAmount 는 0 이상이어야 한다");
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
                            && item.ratio().compareTo(RATIO_TOTAL) <= 0,
                    "ratio 가 0~100 범위를 벗어났다: " + item.ratio());
            ratioSum = ratioSum.add(item.ratio());
        }
        require(ratioSum.subtract(RATIO_TOTAL).abs().compareTo(RATIO_TOLERANCE) <= 0,
                "ratio 의 합이 100 에서 벗어났다: " + ratioSum);

        List<String> insights = analysis.keyInsights();
        require(insights != null && insights.size() == REQUIRED_INSIGHT_COUNT,
                "keyInsights 는 %d개여야 한다".formatted(REQUIRED_INSIGHT_COUNT));
        require(insights.stream().allMatch(this::hasText), "keyInsights 에 빈 문장이 있다");
        require(new HashSet<>(insights).size() == REQUIRED_INSIGHT_COUNT, "keyInsights 가 중복됐다");

        SpendingMissionAnalyzeResponse.MissionPayload mission = response.mission();
        require(mission != null, "mission 이 없다");
        require(hasText(mission.title()), "미션 title 이 비어 있다");
        require(hasText(mission.description()), "미션 description 이 비어 있다");
        require(hasText(mission.reason()), "미션 reason 이 비어 있다");
        require(mission.durationDays() != null && mission.durationDays() > 0,
                "durationDays 는 0보다 커야 한다");
        require(mission.targetAmount() != null
                        && mission.targetAmount() >= TARGET_AMOUNT_MIN
                        && mission.targetAmount() <= TARGET_AMOUNT_MAX,
                "targetAmount 는 5,000~50,000원이어야 한다: " + mission.targetAmount());
        require(mission.targetAmount() % TARGET_AMOUNT_UNIT == 0,
                "targetAmount 는 5,000원 단위여야 한다: " + mission.targetAmount());
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
