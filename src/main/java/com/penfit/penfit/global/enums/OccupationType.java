package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 직업·고용형태 7개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum OccupationType {

    REGULAR_EMPLOYEE("정규직"),
    CONTRACT_EMPLOYEE("계약직"),
    FREELANCER("프리랜서"),
    SELF_EMPLOYED("자영업자"),
    PUBLIC_OFFICIAL("공무원"),
    UNEMPLOYED("무직"),
    OTHER("기타");

    private final String displayName;
}
