package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DebtBand implements DisplayNamed {

    DEBT_NONE("없음"),
    DEBT_LT_10M("1,000만원 미만"),
    DEBT_10M_30M("1,000~3,000만원"),
    DEBT_GE_30M("3,000만원 이상");

    private final String displayName;
}
