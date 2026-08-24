package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {

    PENSION_SAVINGS_FUND("연금저축펀드"),
    INDIVIDUAL_IRP("개인형 IRP"),
    PENSION_SAVINGS_INSURANCE("연금저축보험");

    private final String displayName;
}
