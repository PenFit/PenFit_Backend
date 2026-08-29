package com.penfit.penfit.domain.product.service;

import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.entity.PensionPlan;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.domain.product.dto.ProductComparisonResponse;
import com.penfit.penfit.domain.product.dto.ProductRecommendationResponse;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.ProductRecommendation;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.domain.product.repository.ProductRecommendationRepository;
import com.penfit.penfit.global.client.ai.dto.ProductRecommendationGenerateRequest;
import com.penfit.penfit.global.client.ai.dto.ProductRecommendationGenerateResponse;
import com.penfit.penfit.global.enums.FitLevel;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductRecommendationStore {

    private static final int MINIMUM_CANDIDATES = 3;
    private static final int MAXIMUM_CANDIDATES = 20;
    private static final int RECOMMENDATION_COUNT = 3;

    private final PensionPlanRepository pensionPlanRepository;
    private final PensionPassportRepository pensionPassportRepository;
    private final PensionProductRepository pensionProductRepository;
    private final ProductRecommendationRepository productRecommendationRepository;

    public record RecommendationContext(
            Long userId,
            Long planId,
            ProductRecommendationGenerateRequest request,
            Set<Long> candidateIds
    ) {
    }

    @Transactional(readOnly = true)
    public RecommendationContext loadContext(Long userId) {
        PensionPlan plan = pensionPlanRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PENSION_PLAN_NOT_FOUND));
        PensionPassport passport = pensionPassportRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSPORT_NOT_FOUND));

        List<PensionProduct> candidates = pensionProductRepository
                .findAllByAccountTypeAndIsActiveTrueOrderByIdAsc(plan.getAccountType());
        if (candidates.size() < MINIMUM_CANDIDATES) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_PRODUCT_CANDIDATES);
        }
        if (candidates.size() > MAXIMUM_CANDIDATES) {
            candidates = candidates.subList(0, MAXIMUM_CANDIDATES);
        }

        return new RecommendationContext(
                userId,
                plan.getId(),
                ProductRecommendationGenerateRequest.of(plan, passport, candidates, RECOMMENDATION_COUNT),
                candidates.stream().map(PensionProduct::getId).collect(Collectors.toSet()));
    }

    @Transactional
    public List<ProductRecommendationResponse> save(RecommendationContext context,
                                                    ProductRecommendationGenerateResponse response) {
        productRecommendationRepository.deleteAllByUserId(context.userId());
        productRecommendationRepository.flush();

        List<ProductRecommendation> saved = productRecommendationRepository.saveAll(
                response.recommendations().stream()
                        .sorted((left, right) -> Integer.compare(left.rank(), right.rank()))
                        .map(item -> ProductRecommendation.builder()
                                .userId(context.userId())
                                .planId(context.planId())
                                .productId(item.productId())
                                .rank(item.rank())
                                .fitScore(item.fitScore())
                                .fitLevel(FitLevel.valueOf(item.fitLevel()))
                                .recommendationReason(item.recommendationReason())
                                .modelVersion(response.modelVersion())
                                .build())
                        .toList());

        Map<Long, PensionProduct> products = productsOf(saved);
        return saved.stream()
                .map(recommendation -> ProductRecommendationResponse.of(
                        recommendation, products.get(recommendation.getProductId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductRecommendationResponse> getMyRecommendations(Long userId) {
        List<ProductRecommendation> recommendations = requireRecommendations(userId);
        Map<Long, PensionProduct> products = productsOf(recommendations);
        return recommendations.stream()
                .map(recommendation -> ProductRecommendationResponse.of(
                        recommendation, products.get(recommendation.getProductId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductComparisonResponse getComparison(Long userId) {
        List<ProductRecommendation> recommendations = requireRecommendations(userId);
        Map<Long, PensionProduct> products = productsOf(recommendations);
        return new ProductComparisonResponse(recommendations.stream()
                .map(recommendation -> ProductComparisonResponse.ComparisonItem.of(
                        recommendation, products.get(recommendation.getProductId())))
                .toList());
    }

    private List<ProductRecommendation> requireRecommendations(Long userId) {
        List<ProductRecommendation> recommendations =
                productRecommendationRepository.findAllByUserIdOrderByRankAsc(userId);
        if (recommendations.isEmpty()) {
            throw new BusinessException(ErrorCode.RECOMMENDATION_NOT_FOUND);
        }
        return recommendations;
    }

    private Map<Long, PensionProduct> productsOf(List<ProductRecommendation> recommendations) {
        return pensionProductRepository
                .findAllByIdInAndIsActiveTrue(recommendations.stream()
                        .map(ProductRecommendation::getProductId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(PensionProduct::getId, Function.identity()));
    }
}
