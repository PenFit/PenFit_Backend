package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType {

    BANK("은행"),
    SECURITIES("증권사"),
    INSURANCE("보험사");

    private final String displayName;
}
