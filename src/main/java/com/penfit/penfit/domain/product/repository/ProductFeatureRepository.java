package com.penfit.penfit.domain.product.repository;

import com.penfit.penfit.domain.product.entity.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {

    List<ProductFeature> findAllByProductIdOrderByDisplayOrderAsc(Long productId);
}
