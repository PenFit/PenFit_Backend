package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest.FinancialProfilePayload;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest.PensionSetupPayload;

public record PensionPlanGenerateRequest(
        FinancialProfilePayload financialProfile,
        PensionSetupPayload currentPensionSetup,
        PensionPassportPayload pensionPassport
) {

    public record PensionPassportPayload(
            String typeCode,
            Long sustainableMonthlyContribution,
            String biggestInterruptionRiskCode,
            String marketRiskLevelCode,
            String typeSummary,
            String analysisSummary
    ) {
    }

    public static PensionPlanGenerateRequest of(FinancialProfile profile, PensionSetup setup,
                                                PensionPassport passport) {
        return new PensionPlanGenerateRequest(
                FinancialProfilePayload.from(profile),
                PensionSetupPayload.from(setup),
                new PensionPassportPayload(
                        passport.getTypeCode().name(),
                        passport.getSustainableMonthlyContribution(),
                        passport.getBiggestInterruptionRiskCode().name(),
                        passport.getMarketRiskLevel().name(),
                        passport.getTypeCode().getDescription(),
                        passport.getSummary()));
    }
}
