package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScenarioCode {

    JOB_CHANGE("이직", 1),
    INDEPENDENCE("독립", 2),
    MARRIAGE("결혼", 3),
    HOME_PURCHASE("주택 구매", 4),
    CHILDBIRTH("출산", 5),
    MARKET_DOWNTURN("시장 하락", 6);

    private final String displayName;
    private final int displayOrder;
}
