package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "penfit.mail")
public record MailProperties(String from, String fromName, boolean enabled, String cron) {
}
