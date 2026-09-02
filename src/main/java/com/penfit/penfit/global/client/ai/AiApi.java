package com.penfit.penfit.global.client.ai;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiApi {

    PASSPORT("/passport"),
    PENSION_PLAN("/plan"),
    PRODUCT_RECOMMENDATION("/product"),
    SPENDING_MISSION("/transaction");

    private final String path;
}
