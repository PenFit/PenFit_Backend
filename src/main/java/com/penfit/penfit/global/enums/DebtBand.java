package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 부채 구간 4개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum DebtBand {

    DEBT_NONE("없음"),
    DEBT_LT_10M("1,000만원 미만"),
    DEBT_10M_30M("1,000~3,000만원"),
    DEBT_GE_30M("3,000만원 이상");

    private final String displayName;
}
