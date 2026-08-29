package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.product.entity.PensionProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record ProductRecommendationGenerateRequest(
        PensionPlanPayload pensionPlan,
        PensionPassportPayload pensionPassport,
        List<ProductCandidatePayload> productCandidates,
        int recommendationCount
) {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    public record PensionPlanPayload(
            String accountType,
            Long monthlyContribution,
            BigDecimal stockRatio,
            BigDecimal bondRatio,
            BigDecimal depositRatio
    ) {
    }

    public record PensionPassportPayload(String typeCode, String marketRiskLevelCode) {
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
                                                          List<PensionProduct> candidates,
                                                          int recommendationCount) {
        return new ProductRecommendationGenerateRequest(
                new PensionPlanPayload(
                        plan.getAccountType().name(),
                        plan.getMonthlyContribution(),
                        plan.getStockRatio(),
                        plan.getBondRatio(),
                        plan.getDepositRatio()),
                new PensionPassportPayload(
                        passport.getTypeCode().name(),
                        passport.getMarketRiskLevel().name()),
                candidates.stream().map(ProductCandidatePayload::from).toList(),
                recommendationCount);
    }
}
