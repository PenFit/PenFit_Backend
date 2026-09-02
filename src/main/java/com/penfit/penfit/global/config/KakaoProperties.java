package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "penfit.kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        List<String> redirectUris,
        String tokenUri,
        String userInfoUri,
        Duration connectTimeout,
        Duration readTimeout
) {
}
