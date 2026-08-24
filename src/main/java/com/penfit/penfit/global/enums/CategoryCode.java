package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
