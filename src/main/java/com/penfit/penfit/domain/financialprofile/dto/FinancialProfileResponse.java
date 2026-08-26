package com.penfit.penfit.domain.financialprofile.dto;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.global.common.CodeName;

public record FinancialProfileResponse(
        CodeName ageBand,
        CodeName occupationType,
        Long monthlySalary,
        CodeName livingExpenseBand,
        CodeName assetBand,
        CodeName debtBand,
        CodeName emergencyFundBand,
        Long monthlySavings,
        Long currentInvestment
) {

    public static FinancialProfileResponse from(FinancialProfile profile) {
        return new FinancialProfileResponse(
                CodeName.of(profile.getAgeBand(), profile.getAgeBand().getDisplayName()),
                CodeName.of(profile.getOccupationType(), profile.getOccupationType().getDisplayName()),
                profile.getMonthlySalary(),
                CodeName.of(profile.getLivingExpenseBand(), profile.getLivingExpenseBand().getDisplayName()),
                CodeName.of(profile.getAssetBand(), profile.getAssetBand().getDisplayName()),
                CodeName.of(profile.getDebtBand(), profile.getDebtBand().getDisplayName()),
                CodeName.of(profile.getEmergencyFundBand(), profile.getEmergencyFundBand().getDisplayName()),
                profile.getMonthlySavings(),
                profile.getCurrentInvestment());
    }
}
