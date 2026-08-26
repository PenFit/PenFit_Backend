package com.penfit.penfit.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public record EmailConsentUpdateRequest(
        @NotNull(message = "수신 동의 여부는 필수입니다.")
        Boolean emailConsent
) {
}
