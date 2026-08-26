package com.penfit.penfit.global.common;

public record CodeName(String code, String displayName) {

    public static CodeName of(Enum<?> value, String displayName) {
        return new CodeName(value.name(), displayName);
    }
}
