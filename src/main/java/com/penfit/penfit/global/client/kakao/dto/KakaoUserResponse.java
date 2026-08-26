package com.penfit.penfit.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    public record KakaoAccount(Profile profile) {
    }

    public record Profile(String nickname) {
    }

    public String nicknameOrNull() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }
}
