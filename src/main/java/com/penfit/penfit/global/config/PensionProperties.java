package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "penfit.pension")
public record PensionProperties(
        BigDecimal expectedReturnRate,
        int contributionYears,
        long minimumMonthlyContribution
) {
}
