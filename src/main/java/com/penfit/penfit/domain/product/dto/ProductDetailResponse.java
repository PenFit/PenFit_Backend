package com.penfit.penfit.domain.product.dto;

import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        CodeName providerType,
        String providerName,
        String productName,
        CodeName accountType,
        CodeName productType,
        String summary,
        String recommendationReason,
        BigDecimal feeMinRate,
        BigDecimal feeMaxRate,
        String investmentScope,
        String officialUrl,
        List<String> features,
        List<String> cautions,
        boolean saved
) {

    public static ProductDetailResponse of(PensionProduct product, List<String> features,
                                           List<String> cautions, String recommendationReason, boolean saved) {
        return new ProductDetailResponse(
                product.getId(),
                CodeName.of(product.getProviderType(), product.getProviderType().getDisplayName()),
                product.getProviderName(),
                product.getProductName(),
                CodeName.of(product.getAccountType(), product.getAccountType().getDisplayName()),
                CodeName.of(product.getProductType(), product.getProductType().getDisplayName()),
                product.getSummary(),
                recommendationReason,
                product.getFeeMinRate(),
                product.getFeeMaxRate(),
                product.getInvestmentScope(),
                product.getOfficialUrl(),
                features,
                cautions,
                saved);
    }
}
