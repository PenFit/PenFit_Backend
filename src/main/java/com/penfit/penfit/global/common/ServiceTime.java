package com.penfit.penfit.global.common;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class ServiceTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private ServiceTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDate toLocalDate(OffsetDateTime dateTime) {
        return dateTime.atZoneSameInstant(ZONE).toLocalDate();
    }
}
