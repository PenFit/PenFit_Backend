package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductType {

    FUND_ACCOUNT("연금저축펀드 계좌", AccountType.PENSION_SAVINGS_FUND),
    IRP_ACCOUNT("개인형 IRP 계좌", AccountType.INDIVIDUAL_IRP),
    INSURANCE_PRODUCT("연금저축보험 상품", AccountType.PENSION_SAVINGS_INSURANCE);

    private final String displayName;
    private final AccountType allowedAccountType;

    public boolean supports(AccountType accountType) {
        return this.allowedAccountType == accountType;
    }
}
