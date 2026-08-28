package com.penfit.penfit.domain.pensionsetup.dto;

import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.global.common.CodeName;

import java.math.BigDecimal;
import java.util.List;

public record PensionSetupResponse(
        CodeName accountType,
        Long monthlyContribution,
        Long previewFutureAsset,
        BigDecimal expectedReturnRate,
        Integer contributionYears,
        List<GrowthPoint> growth
) {

    public record GrowthPoint(int years, long futureAsset) {
    }

    public static PensionSetupResponse of(PensionSetup setup, List<GrowthPoint> growth) {
        return new PensionSetupResponse(
                CodeName.of(setup.getAccountType(), setup.getAccountType().getDisplayName()),
                setup.getMonthlyContribution(),
                setup.getPreviewFutureAsset(),
                setup.getExpectedReturnRate(),
                setup.getContributionYears(),
                growth);
    }
}
