package com.penfit.penfit.domain.pensionplan.dto;

import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlanAdvantage;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PensionPlanResponse(
        Long planId,
        String planName,
        CodeName accountType,
        Long monthlyContribution,
        Long expectedFutureAsset,
        Integer contributionYears,
        BigDecimal expectedReturnRate,
        AssetAllocationResponse assetAllocation,
        List<String> advantages,
        String recommendationReason,
        OffsetDateTime createdAt
) {

    public record AssetAllocationResponse(
            BigDecimal stockRatio,
            BigDecimal bondRatio,
            BigDecimal depositRatio
    ) {
    }

    public static PensionPlanResponse of(PensionPlan plan, List<PensionPlanAdvantage> advantages) {
        return new PensionPlanResponse(
                plan.getId(),
                plan.getPlanName(),
                CodeName.of(plan.getAccountType(), plan.getAccountType().getDisplayName()),
                plan.getMonthlyContribution(),
                plan.getExpectedFutureAsset(),
                plan.getContributionYears(),
                plan.getExpectedReturnRate(),
                new AssetAllocationResponse(plan.getStockRatio(), plan.getBondRatio(), plan.getDepositRatio()),
                advantages.stream().map(PensionPlanAdvantage::getContent).toList(),
                plan.getRecommendationReason(),
                plan.getCreatedAt());
    }
}
