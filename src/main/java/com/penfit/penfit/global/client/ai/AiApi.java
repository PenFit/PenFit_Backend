package com.penfit.penfit.global.client.ai;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiApi {

    PASSPORT("/internal/v1/pension-passport/analyze"),
    PENSION_PLAN("/internal/v1/pension-plan/generate"),
    PRODUCT_RECOMMENDATION("/internal/v1/product-recommendations/generate"),
    SPENDING_MISSION("/internal/v1/spending-mission/analyze");

    private final String path;
}
