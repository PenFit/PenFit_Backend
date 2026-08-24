package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LivingExpenseBand {

    LIVING_LE_1M("100만원 이하"),
    LIVING_GT_1M_LE_1_5M("100~150만원"),
    LIVING_GT_1_5M_LE_2M("150~200만원"),
    LIVING_GT_2M("200만원 초과");

    private final String displayName;
}
