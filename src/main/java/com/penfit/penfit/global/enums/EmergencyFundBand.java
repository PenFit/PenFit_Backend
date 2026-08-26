package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmergencyFundBand implements DisplayNamed {

    EMERGENCY_LT_1M("100만원 미만"),
    EMERGENCY_1M_3M("100~300만원"),
    EMERGENCY_3M_5M("300~500만원"),
    EMERGENCY_GE_5M("500만원 이상");

    private final String displayName;
}
