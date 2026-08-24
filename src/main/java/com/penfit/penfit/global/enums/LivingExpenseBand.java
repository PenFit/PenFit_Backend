package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 월 생활비 구간 4개. LE는 이하, GT는 초과를 뜻한다.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum LivingExpenseBand {

    LIVING_LE_1M("100만원 이하"),
    LIVING_GT_1M_LE_1_5M("100~150만원"),
    LIVING_GT_1_5M_LE_2M("150~200만원"),
    LIVING_GT_2M("200만원 초과");

    private final String displayName;
}
