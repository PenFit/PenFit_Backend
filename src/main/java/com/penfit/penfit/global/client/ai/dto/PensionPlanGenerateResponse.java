package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record PensionPlanGenerateResponse(
        String planName,
        String accountType,
        Long monthlyContribution,
        AssetAllocation assetAllocation,
        List<String> advantages,
        String recommendationReason,
        String modelVersion
) {

    public record AssetAllocation(BigDecimal stockRatio, BigDecimal bondRatio, BigDecimal depositRatio) {
    }
}
