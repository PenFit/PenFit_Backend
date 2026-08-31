package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassportTypeCode implements DisplayNamed {

    STEADY_PIONEER("성실한 개척자형", "연금 납입을 비교적 꾸준히 유지하는 유형"),
    FLEXIBLE_MAINTAINER("유연한 유지형", "상황에 맞게 납입액을 조절하면서 연금을 유지하는 유형"),
    CASHFLOW_GUARDIAN("현금흐름 방어형", "현재 생활비·현금흐름을 우선적으로 보호하는 유형"),
    MARKET_SENSITIVE("시장 반응형", "시장 변동에 따라 연금 행동이 영향을 받는 유형"),
    LONG_TERM_KEEPER("장기 유지형", "장기적인 연금 유지를 중요하게 보는 유형");

    private final String displayName;
    private final String description;
}
