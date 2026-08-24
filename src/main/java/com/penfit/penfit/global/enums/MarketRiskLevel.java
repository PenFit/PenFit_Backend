package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarketRiskLevel {

    LOW("낮음"),
    MEDIUM("중간"),
    HIGH("높음");

    private final String displayName;
}
