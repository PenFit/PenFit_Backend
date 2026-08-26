package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeBand implements DisplayNamed {

    AGE_23_25("20대 초반 (23~25세)"),
    AGE_26_28("20대 중반 (26~28세)"),
    AGE_29_31("20대 후반 (29~31세)"),
    AGE_32_34("30대 초반 (32~34세)");

    private final String displayName;
}
