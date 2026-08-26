package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OccupationType implements DisplayNamed {

    REGULAR_EMPLOYEE("정규직"),
    CONTRACT_EMPLOYEE("계약직"),
    FREELANCER("프리랜서"),
    SELF_EMPLOYED("자영업자"),
    PUBLIC_OFFICIAL("공무원"),
    UNEMPLOYED("무직"),
    OTHER("기타");

    private final String displayName;
}
