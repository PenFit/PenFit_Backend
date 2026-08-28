package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "penfit.ai")
public record AiProperties(
        String baseUrl,
        String apiKey,
        Duration connectTimeout,
        Timeout timeout
) {

    public record Timeout(
            Duration passport,
            Duration pensionPlan,
            Duration productRecommendation,
            Duration spendingMission
    ) {
    }
}
