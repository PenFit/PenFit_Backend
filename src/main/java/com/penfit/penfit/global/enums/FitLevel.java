package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FitLevel implements DisplayNamed {

    VERY_HIGH("매우 높음"),
    HIGH("높음"),
    MEDIUM("중간"),
    LOW("낮음");

    private final String displayName;
}
