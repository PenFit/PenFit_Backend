package com.penfit.penfit.domain.product.service;

import com.penfit.penfit.domain.product.dto.ProductComparisonResponse;
import com.penfit.penfit.domain.product.dto.ProductRecommendationResponse;
import com.penfit.penfit.domain.product.service.ProductRecommendationStore.RecommendationContext;
import com.penfit.penfit.global.client.ai.AiApi;
import com.penfit.penfit.global.client.ai.AiClient;
import com.penfit.penfit.global.client.ai.dto.ProductRecommendationGenerateResponse;
import com.penfit.penfit.global.enums.FitLevel;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRecommendationService {

    private static final int RECOMMENDATION_COUNT = 3;
    private static final Set<Integer> REQUIRED_RANKS = Set.of(1, 2, 3);

    private final AiClient aiClient;
    private final ProductRecommendationStore productRecommendationStore;

    public List<ProductRecommendationResponse> create(Long userId) {
        RecommendationContext context = productRecommendationStore.loadContext(userId);

        ProductRecommendationGenerateResponse response = aiClient.call(
                AiApi.PRODUCT_RECOMMENDATION, context.request(),
                ProductRecommendationGenerateResponse.class, userId);
        validate(response, context);

        return productRecommendationStore.save(context, response);
    }

    public List<ProductRecommendationResponse> getMyRecommendations(Long userId) {
        return productRecommendationStore.getMyRecommendations(userId);
    }

    public ProductComparisonResponse getComparison(Long userId) {
        return productRecommendationStore.getComparison(userId);
    }

    private void validate(ProductRecommendationGenerateResponse response, RecommendationContext context) {
        require(response.recommendedProducts() != null
                        && response.recommendedProducts().size() == RECOMMENDATION_COUNT,
                "recommendedProducts 는 %d개여야 한다".formatted(RECOMMENDATION_COUNT));
        require(hasText(response.modelVersion()), "modelVersion 이 비어 있다");

        Set<Long> productIds = new HashSet<>();
        Set<Integer> ranks = new HashSet<>();
        for (ProductRecommendationGenerateResponse.RecommendedProduct item : response.recommendedProducts()) {
            require(item.productId() != null && context.candidateIds().contains(item.productId()),
                    "요청한 후보에 없는 productId 다: " + item.productId());
            require(productIds.add(item.productId()), "같은 상품이 중복됐다: " + item.productId());
            require(item.rank() != null && ranks.add(item.rank()), "rank 가 중복됐다: " + item.rank());
            require(inScoreRange(item.fitScore()), "fitScore 는 0~1 이어야 한다: " + item.fitScore());
            require(isFitLevel(item.fitLevel()), "알 수 없는 fitLevel: " + item.fitLevel());
            require(hasText(item.recommendationReason()), "recommendationReason 이 비어 있다");
        }
        require(ranks.equals(REQUIRED_RANKS), "rank 는 1, 2, 3 이어야 한다: " + ranks);
    }

    private boolean inScoreRange(BigDecimal fitScore) {
        return fitScore != null
                && fitScore.compareTo(BigDecimal.ZERO) >= 0
                && fitScore.compareTo(BigDecimal.ONE) <= 0;
    }

    private boolean isFitLevel(String value) {
        if (value == null) {
            return false;
        }
        try {
            FitLevel.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void require(boolean condition, String reason) {
        if (!condition) {
            log.warn("상품 추천 응답 검증 실패 reason={}", reason);
            throw new BusinessException(ErrorCode.AI_INVALID_RESPONSE);
        }
    }
}
