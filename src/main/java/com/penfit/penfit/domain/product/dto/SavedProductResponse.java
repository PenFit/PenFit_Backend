package com.penfit.penfit.domain.product.dto;

import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SavedProductResponse(
        Long productId,
        CodeName accountType,
        String providerName,
        String productName,
        String summary,
        BigDecimal feeMinRate,
        BigDecimal feeMaxRate,
        String investmentScope,
        OffsetDateTime savedAt
) {

    public static SavedProductResponse of(PensionProduct product, OffsetDateTime savedAt) {
        return new SavedProductResponse(
                product.getId(),
                CodeName.of(product.getAccountType(), product.getAccountType().getDisplayName()),
                product.getProviderName(),
                product.getProductName(),
                product.getSummary(),
                product.getFeeMinRate(),
                product.getFeeMaxRate(),
                product.getInvestmentScope(),
                savedAt);
    }
}
