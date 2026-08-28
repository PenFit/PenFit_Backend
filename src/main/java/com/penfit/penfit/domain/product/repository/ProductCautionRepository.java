package com.penfit.penfit.domain.product.repository;

import com.penfit.penfit.domain.product.entity.ProductCaution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCautionRepository extends JpaRepository<ProductCaution, Long> {

    List<ProductCaution> findAllByProductIdOrderByDisplayOrderAsc(Long productId);
}
