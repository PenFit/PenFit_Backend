package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record SpendingMissionAnalyzeResponse(
        SpendingAnalysisPayload spendingAnalysis,
        MissionPayload mission,
        String modelVersion
) {

    public record SpendingAnalysisPayload(
            String topCategoryCode,
            Long recurringExpense,
            Long reducibleAmount,
            List<CategorySpending> categorySpending,
            List<String> keyInsights,
            String summary
    ) {
    }

    public record CategorySpending(String categoryCode, Long amount, BigDecimal ratio) {
    }

    public record MissionPayload(
            String title,
            String description,
            Long targetAmount,
            Integer durationDays,
            String reason
    ) {
    }
}
