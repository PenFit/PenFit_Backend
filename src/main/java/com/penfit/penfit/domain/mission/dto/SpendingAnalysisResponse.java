package com.penfit.penfit.domain.mission.dto;

import com.penfit.penfit.domain.mission.entity.SpendingAnalysis;
import com.penfit.penfit.domain.mission.entity.SpendingCategoryAmount;
import com.penfit.penfit.domain.mission.entity.SpendingKeyInsight;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SpendingAnalysisResponse(
        Long analysisId,
        LocalDate analysisStartDate,
        LocalDate analysisEndDate,
        CodeName topCategory,
        Long totalAmount,
        Long recurringExpense,
        Long reducibleAmount,
        String summary,
        List<CategorySpendingResponse> categorySpending,
        List<String> keyInsights
) {

    public record CategorySpendingResponse(CodeName category, Long amount, BigDecimal ratio) {
    }

    public static SpendingAnalysisResponse of(SpendingAnalysis analysis,
                                              List<CategorySpendingResponse> categorySpending,
                                              List<SpendingKeyInsight> insights) {
        long totalAmount = categorySpending.stream()
                .mapToLong(CategorySpendingResponse::amount)
                .sum();

        return new SpendingAnalysisResponse(
                analysis.getId(),
                analysis.getAnalysisStartDate(),
                analysis.getAnalysisEndDate(),
                CodeName.of(analysis.getTopCategoryCode(), analysis.getTopCategoryCode().getDisplayName()),
                totalAmount,
                analysis.getRecurringExpense(),
                analysis.getReducibleAmount(),
                analysis.getSummary(),
                categorySpending,
                insights.stream().map(SpendingKeyInsight::getContent).toList());
    }

    public static CategorySpendingResponse item(SpendingCategoryAmount amount) {
        return new CategorySpendingResponse(
                CodeName.of(amount.getCategoryCode(), amount.getCategoryCode().getDisplayName()),
                amount.getAmount(),
                amount.getRatio());
    }
}
