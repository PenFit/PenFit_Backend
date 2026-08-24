package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소비 카테고리 5개.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum CategoryCode {

    FOOD_DELIVERY("외식·배달"),
    SHOPPING("쇼핑"),
    TRANSPORTATION("교통"),
    SUBSCRIPTION("구독"),
    OTHER("기타");

    private final String displayName;
}
