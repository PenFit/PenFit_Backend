package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "penfit.cookie")
public record CookieProperties(
        String refreshTokenName,
        String path,
        boolean secure,
        String sameSite
) {
}
