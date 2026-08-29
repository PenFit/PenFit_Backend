package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionStatus implements DisplayNamed {

    PENDING("시작 전"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    EXPIRED("기간 만료");

    private final String displayName;
}
