package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 비상금 구간 4개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum EmergencyFundBand {

    EMERGENCY_LT_1M("100만원 미만"),
    EMERGENCY_1M_3M("100~300만원"),
    EMERGENCY_3M_5M("300~500만원"),
    EMERGENCY_GE_5M("500만원 이상");

    private final String displayName;
}
