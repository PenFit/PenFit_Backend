package com.penfit.penfit.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        Long userId,
        String nickname,
        boolean newUser
) {
}
