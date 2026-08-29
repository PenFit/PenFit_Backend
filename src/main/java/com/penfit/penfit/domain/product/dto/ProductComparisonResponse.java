package com.penfit.penfit.domain.product.dto;

import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.ProductRecommendation;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.util.List;

public record ProductComparisonResponse(List<ComparisonItem> products) {

    public record ComparisonItem(
            int rank,
            Long productId,
            String providerName,
            String productName,
            BigDecimal feeMinRate,
            BigDecimal feeMaxRate,
            String investmentScope,
            CodeName fitLevel
    ) {

        public static ComparisonItem of(ProductRecommendation recommendation, PensionProduct product) {
            return new ComparisonItem(
                    recommendation.getRank(),
                    product.getId(),
                    product.getProviderName(),
                    product.getProductName(),
                    product.getFeeMinRate(),
                    product.getFeeMaxRate(),
                    product.getInvestmentScope(),
                    CodeName.of(recommendation.getFitLevel(),
                            recommendation.getFitLevel().getDisplayName()));
        }
    }
}
