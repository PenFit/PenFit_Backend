package com.penfit.penfit.domain.financialprofile.dto;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.global.enums.AgeBand;
import com.penfit.penfit.global.enums.AssetBand;
import com.penfit.penfit.global.enums.DebtBand;
import com.penfit.penfit.global.enums.EmergencyFundBand;
import com.penfit.penfit.global.enums.LivingExpenseBand;
import com.penfit.penfit.global.enums.OccupationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FinancialProfileCreateRequest(
        @NotNull(message = "나이 구간은 필수입니다.")
        AgeBand ageBand,

        @NotNull(message = "직업 유형은 필수입니다.")
        OccupationType occupationType,

        @NotNull(message = "월급은 필수입니다.")
        @PositiveOrZero(message = "월급은 0원 이상이어야 합니다.")
        Long monthlySalary,

        @NotNull(message = "월 생활비 구간은 필수입니다.")
        LivingExpenseBand livingExpenseBand,

        @NotNull(message = "자산 구간은 필수입니다.")
        AssetBand assetBand,

        @NotNull(message = "부채 구간은 필수입니다.")
        DebtBand debtBand,

        @NotNull(message = "비상금 구간은 필수입니다.")
        EmergencyFundBand emergencyFundBand,

        @NotNull(message = "월 저축금액은 필수입니다.")
        @PositiveOrZero(message = "월 저축금액은 0원 이상이어야 합니다.")
        Long monthlySavings,

        @NotNull(message = "현재 투자금액은 필수입니다.")
        @PositiveOrZero(message = "현재 투자금액은 0원 이상이어야 합니다.")
        Long currentInvestment
) {

    public FinancialProfile toEntity(Long userId) {
        return FinancialProfile.builder()
                .userId(userId)
                .ageBand(ageBand)
                .occupationType(occupationType)
                .monthlySalary(monthlySalary)
                .livingExpenseBand(livingExpenseBand)
                .assetBand(assetBand)
                .debtBand(debtBand)
                .emergencyFundBand(emergencyFundBand)
                .monthlySavings(monthlySavings)
                .currentInvestment(currentInvestment)
                .build();
    }
}
