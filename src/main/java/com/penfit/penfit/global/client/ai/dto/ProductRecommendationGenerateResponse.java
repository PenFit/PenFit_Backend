package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductRecommendationGenerateResponse(
        List<RecommendedProduct> recommendedProducts,
        String modelVersion
) {

    public record RecommendedProduct(
            Integer rank,
            Long productId,
            BigDecimal fitScore,
            String fitLevel,
            String recommendationReason
    ) {
    }
}
