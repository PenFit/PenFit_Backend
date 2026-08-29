package com.penfit.penfit.domain.product.dto;

import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.ProductRecommendation;
import com.penfit.penfit.global.common.CodeName;

public record ProductRecommendationResponse(
        int rank,
        Long productId,
        CodeName accountType,
        String providerName,
        String productName,
        String summary,
        CodeName fitLevel,
        String recommendationReason
) {

    public static ProductRecommendationResponse of(ProductRecommendation recommendation,
                                                   PensionProduct product) {
        return new ProductRecommendationResponse(
                recommendation.getRank(),
                product.getId(),
                CodeName.of(product.getAccountType(), product.getAccountType().getDisplayName()),
                product.getProviderName(),
                product.getProductName(),
                product.getSummary(),
                CodeName.of(recommendation.getFitLevel(), recommendation.getFitLevel().getDisplayName()),
                recommendation.getRecommendationReason());
    }
}
