package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductRecommendationGenerateResponse(
        List<Recommendation> recommendations,
        String modelVersion
) {

    public record Recommendation(
            Long productId,
            Integer rank,
            BigDecimal fitScore,
            String fitLevel,
            String recommendationReason
    ) {
    }
}
