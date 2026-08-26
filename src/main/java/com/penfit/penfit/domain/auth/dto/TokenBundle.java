package com.penfit.penfit.domain.auth.dto;

public record TokenBundle(
        String accessToken,
        String refreshToken,
        long refreshTokenMaxAgeSeconds
) {
}
