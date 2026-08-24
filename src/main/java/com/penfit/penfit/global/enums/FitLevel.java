package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 추천 적합도 4단계. 상품 비교 화면에 그대로 노출된다.
 * Enum 단일 기준 명세서를 따른다. 코드 문자열은 enum 이름과 같다.
 */
@Getter
@RequiredArgsConstructor
public enum FitLevel {

    VERY_HIGH("매우 높음"),
    HIGH("높음"),
    MEDIUM("중간"),
    LOW("낮음");

    private final String displayName;
}
