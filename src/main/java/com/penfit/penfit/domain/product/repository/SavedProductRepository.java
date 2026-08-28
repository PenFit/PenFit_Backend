package com.penfit.penfit.domain.product.repository;

import com.penfit.penfit.domain.product.entity.SavedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedProductRepository extends JpaRepository<SavedProduct, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<SavedProduct> findAllByUserIdOrderBySavedAtDesc(Long userId);

    long deleteByUserIdAndProductId(Long userId, Long productId);
}
