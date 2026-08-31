package com.penfit.penfit.domain.home.dto;

import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.global.common.CodeName;
import com.penfit.penfit.global.common.ServiceTime;
import com.penfit.penfit.global.enums.MissionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record HomeResponse(
        String nickname,
        PassportCard passport,
        PensionPlanCard pensionPlan,
        MissionCard mission,
        List<SavedProductCard> savedProducts
) {

    public record PassportCard(CodeName type, String typeSummary) {

        public static PassportCard of(PensionPassport passport) {
            return new PassportCard(
                    CodeName.of(passport.getTypeCode(), passport.getTypeCode().getDisplayName()),
                    passport.getTypeSummary());
        }
    }

    public record PensionPlanCard(
            Long planId,
            String planName,
            Long monthlyContribution,
            CodeName accountType,
            Long expectedFutureAsset,
            Integer contributionYears
    ) {

        public static PensionPlanCard of(PensionPlan plan) {
            return new PensionPlanCard(
                    plan.getId(),
                    plan.getPlanName(),
                    plan.getMonthlyContribution(),
                    CodeName.of(plan.getAccountType(), plan.getAccountType().getDisplayName()),
                    plan.getExpectedFutureAsset(),
                    plan.getContributionYears());
        }
    }

    public record MissionCard(
            Long missionId,
            String title,
            String description,
            Long targetAmount,
            LocalDate dueDate,
            long daysLeft,
            CodeName status
    ) {

        public static MissionCard of(BehaviorMission mission) {
            LocalDate today = ServiceTime.today();
            MissionStatus status = mission.isExpired(today) ? MissionStatus.EXPIRED : mission.getStatus();

            return new MissionCard(
                    mission.getId(),
                    mission.getTitle(),
                    mission.getDescription(),
                    mission.getTargetAmount(),
                    mission.getDueDate(),
                    Math.max(ChronoUnit.DAYS.between(today, mission.getDueDate()), 0),
                    CodeName.of(status, status.getDisplayName()));
        }
    }

    public record SavedProductCard(
            Long productId,
            String productName,
            String providerName,
            String investmentScope,
            BigDecimal feeMinRate,
            BigDecimal feeMaxRate
    ) {

        public static SavedProductCard of(PensionProduct product) {
            return new SavedProductCard(
                    product.getId(),
                    product.getProductName(),
                    product.getProviderName(),
                    product.getInvestmentScope(),
                    product.getFeeMinRate(),
                    product.getFeeMaxRate());
        }
    }
}
