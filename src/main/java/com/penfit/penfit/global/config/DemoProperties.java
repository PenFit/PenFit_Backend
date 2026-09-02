package com.penfit.penfit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "penfit.demo")
public record DemoProperties(boolean enabled, String nickname) {
}
