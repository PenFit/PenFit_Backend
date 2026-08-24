package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassportTypeCode {

    STEADY_PIONEER("성실한 개척자형", "생애 변화와 시장 하락에도 납입을 유지하거나 늘리려는 경향"),
    FLEXIBLE_BALANCER("유연한 균형형", "계좌는 유지하면서 상황에 따라 납입액을 감액·일시중단하는 경향"),
    CAUTIOUS_GUARDIAN("신중한 수호자형", "계좌 유지와 함께 리밸런싱·안전자산 확대를 선호하는 경향"),
    REALISTIC_PLANNER("현실적인 설계자형", "단기 생애목표와 현금흐름을 우선하며 중단·재설계를 선택하는 경향");

    private final String displayName;
    private final String description;
}
