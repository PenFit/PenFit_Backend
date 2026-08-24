package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리허설 진행 상태. ANALYZING 동안 프론트가 2초 간격으로 폴링하고,
 * COMPLETED 또는 FAILED가 되면 폴링을 멈춘다.
 */
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
