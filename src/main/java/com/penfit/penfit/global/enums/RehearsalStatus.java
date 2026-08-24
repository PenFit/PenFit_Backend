package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RehearsalStatus {

    IN_PROGRESS("진행 중"),
    ANALYZING("AI 분석 중"),
    COMPLETED("완료"),
    FAILED("분석 실패");

    private final String displayName;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
