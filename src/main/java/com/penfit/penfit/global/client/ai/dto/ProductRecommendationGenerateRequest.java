package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.global.client.ai.dto.PensionPlanGenerateRequest.MarketRiskPayload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record ProductRecommendationGenerateRequest(
        PassportPayload passport,
        PlanPayload plan,
        List<ProductCandidatePayload> products
) {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    public record PassportPayload(
            String typeCode,
            String typeName,
            Long sustainableMonthlyContribution,
            MarketRiskPayload marketRiskLevel
    ) {
    }

    public record PlanPayload(
            Long monthlyContribution,
            AllocationPayload allocation,
            String targetAccountType
    ) {
    }

    public record AllocationPayload(BigDecimal stockRatio, BigDecimal bondRatio, BigDecimal depositRatio) {
    }

    public record ProductCandidatePayload(
            Long productId,
            String providerType,
            String providerName,
            String productName,
            String accountType,
            String productType,
            BigDecimal feeMinRate,
            BigDecimal feeMaxRate,
            String summary
    ) {

        public static ProductCandidatePayload from(PensionProduct product) {
            return new ProductCandidatePayload(
                    product.getId(),
                    product.getProviderType().name(),
                    product.getProviderName(),
                    product.getProductName(),
                    product.getAccountType().name(),
                    product.getProductType().name(),
                    toPercent(product.getFeeMinRate()),
                    toPercent(product.getFeeMaxRate()),
                    product.getSummary());
        }

        private static BigDecimal toPercent(BigDecimal ratio) {
            return ratio.multiply(PERCENT).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        }
    }

    public static ProductRecommendationGenerateRequest of(PensionPlan plan, PensionPassport passport,
                                                          List<PensionProduct> candidates) {
        return new ProductRecommendationGenerateRequest(
                new PassportPayload(
                        passport.getTypeCode().name(),
                        passport.getTypeCode().getDisplayName(),
                        passport.getSustainableMonthlyContribution(),
                        new MarketRiskPayload(
                                passport.getMarketRiskLevel().name(),
                                passport.getMarketRiskLevel().getDisplayName())),
                new PlanPayload(
                        plan.getMonthlyContribution(),
                        new AllocationPayload(plan.getStockRatio(), plan.getBondRatio(), plan.getDepositRatio()),
                        plan.getAccountType().name()),
                candidates.stream().map(ProductCandidatePayload::from).toList());
    }
}
