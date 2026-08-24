package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 가상 연금계좌 종류 3개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum AccountType {

    PENSION_SAVINGS_FUND("연금저축펀드"),
    INDIVIDUAL_IRP("개인형 IRP"),
    PENSION_SAVINGS_INSURANCE("연금저축보험");

    private final String displayName;
}
