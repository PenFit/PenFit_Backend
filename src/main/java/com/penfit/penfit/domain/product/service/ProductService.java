package com.penfit.penfit.domain.product.service;

import com.penfit.penfit.domain.product.dto.ProductDetailResponse;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.ProductCaution;
import com.penfit.penfit.domain.product.entity.ProductFeature;
import com.penfit.penfit.domain.product.entity.ProductRecommendation;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.domain.product.repository.ProductCautionRepository;
import com.penfit.penfit.domain.product.repository.ProductFeatureRepository;
import com.penfit.penfit.domain.product.repository.ProductRecommendationRepository;
import com.penfit.penfit.domain.product.repository.SavedProductRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final PensionProductRepository pensionProductRepository;
    private final ProductFeatureRepository productFeatureRepository;
    private final ProductCautionRepository productCautionRepository;
    private final ProductRecommendationRepository productRecommendationRepository;
    private final SavedProductRepository savedProductRepository;

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Long userId, Long productId) {
        PensionProduct product = pensionProductRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        List<String> features = productFeatureRepository.findAllByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(ProductFeature::getContent)
                .toList();

        List<String> cautions = productCautionRepository.findAllByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(ProductCaution::getContent)
                .toList();

        String recommendationReason = productRecommendationRepository.findByUserIdAndProductId(userId, productId)
                .map(ProductRecommendation::getRecommendationReason)
                .orElse(null);

        boolean saved = savedProductRepository.existsByUserIdAndProductId(userId, productId);

        return ProductDetailResponse.of(product, features, cautions, recommendationReason, saved);
    }
}
