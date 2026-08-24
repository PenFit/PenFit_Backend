package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeBand {

    AGE_23_25("23~25세"),
    AGE_26_28("26~28세"),
    AGE_29_31("29~31세"),
    AGE_32_34("32~34세");

    private final String displayName;
}
