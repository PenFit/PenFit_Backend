package com.penfit.penfit.domain.user.dto;

import com.penfit.penfit.domain.user.entity.User;

public record UserInfoResponse(
        Long userId,
        String nickname,
        String email,
        boolean emailConsent,
        String loginProvider
) {

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.isEmailConsent(),
                "KAKAO");
    }
}
