package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 금융회사 종류 3개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum ProviderType {

    BANK("은행"),
    SECURITIES("증권사"),
    INSURANCE("보험사");

    private final String displayName;
}
