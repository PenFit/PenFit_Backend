package com.penfit.penfit.domain.product.repository;

import com.penfit.penfit.domain.product.entity.ProductRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {

    Optional<ProductRecommendation> findByUserIdAndProductId(Long userId, Long productId);
}
