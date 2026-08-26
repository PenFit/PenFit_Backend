package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "penfit.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
}
