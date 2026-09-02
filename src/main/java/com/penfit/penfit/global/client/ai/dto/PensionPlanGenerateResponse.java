package com.penfit.penfit.global.client.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record PensionPlanGenerateResponse(
        String title,
        Long monthlyContribution,
        Allocation allocation,
        String targetAccountType,
        List<String> advantages,
        String recommendationReason,
        String modelVersion
) {

    public record Allocation(BigDecimal stockRatio, BigDecimal bondRatio, BigDecimal depositRatio) {
    }
}
