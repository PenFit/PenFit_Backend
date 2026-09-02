package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record SpendingMissionAnalyzeResponse(
        WeeklySpendingAnalysis weeklySpendingAnalysis,
        WeeklyMission weeklyMission,
        String modelVersion
) {

    public record WeeklySpendingAnalysis(
            Long totalSpending,
            Integer transactionCount,
            String topCategory,
            List<CategorySpending> categorySpending,
            List<String> insights,
            String summary
    ) {
    }

    public record CategorySpending(String categoryCode, Long amount, BigDecimal ratio) {
    }

    public record WeeklyMission(
            String missionCode,
            String title,
            String description,
            String targetCategory,
            Long targetAmount,
            String period,
            String reason
    ) {
    }
}
