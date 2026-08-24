package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 패스포트의 시장 위험도 3단계.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum MarketRiskLevel {

    LOW("낮음"),
    MEDIUM("중간"),
    HIGH("높음");

    private final String displayName;
}
