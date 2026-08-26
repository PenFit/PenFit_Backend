package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "penfit.kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String tokenUri,
        String userInfoUri,
        Duration connectTimeout,
        Duration readTimeout
) {
}
