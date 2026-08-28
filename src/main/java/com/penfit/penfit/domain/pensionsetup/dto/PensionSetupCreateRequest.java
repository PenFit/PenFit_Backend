package com.penfit.penfit.domain.pensionsetup.dto;

import com.penfit.penfit.global.enums.AccountType;
import jakarta.validation.constraints.NotNull;

public record PensionSetupCreateRequest(
        @NotNull(message = "연금계좌 종류는 필수입니다.")
        AccountType accountType,

        @NotNull(message = "월 납입액은 필수입니다.")
        Long monthlyContribution
) {
}
