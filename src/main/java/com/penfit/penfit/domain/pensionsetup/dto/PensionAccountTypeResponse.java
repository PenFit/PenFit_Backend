package com.penfit.penfit.domain.pensionsetup.dto;

import java.util.List;

public record PensionAccountTypeResponse(
        String code,
        String name,
        String title,
        String description,
        List<String> tags,
        Comparison comparison
) {

    public record Comparison(
            String investmentStyle,
            String taxBenefit,
            String keyFeature,
            String recommendedFor
    ) {
    }
}
