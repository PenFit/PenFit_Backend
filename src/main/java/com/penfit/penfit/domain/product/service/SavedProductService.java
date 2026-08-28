package com.penfit.penfit.domain.product.service;

import com.penfit.penfit.domain.product.dto.SavedProductResponse;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.SavedProduct;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.domain.product.repository.SavedProductRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedProductService {

    private final SavedProductRepository savedProductRepository;
    private final PensionProductRepository pensionProductRepository;

    @Transactional
    public void save(Long userId, Long productId) {
        if (!pensionProductRepository.existsByIdAndIsActiveTrue(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (savedProductRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException(ErrorCode.SAVED_PRODUCT_ALREADY_EXISTS);
        }
        savedProductRepository.save(SavedProduct.builder()
                .userId(userId)
                .productId(productId)
                .build());
    }

    @Transactional(readOnly = true)
    public List<SavedProductResponse> getMySavedProducts(Long userId) {
        List<SavedProduct> saved = savedProductRepository.findAllByUserIdOrderBySavedAtDesc(userId);
        if (saved.isEmpty()) {
            return List.of();
        }

        Map<Long, PensionProduct> products = pensionProductRepository
                .findAllByIdInAndIsActiveTrue(saved.stream().map(SavedProduct::getProductId).toList())
                .stream()
                .collect(Collectors.toMap(PensionProduct::getId, Function.identity()));

        return saved.stream()
                .filter(item -> products.containsKey(item.getProductId()))
                .map(item -> SavedProductResponse.of(products.get(item.getProductId()), item.getSavedAt()))
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long productId) {
        savedProductRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
