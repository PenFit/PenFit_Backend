package com.penfit.penfit.global.client.ai.dto;

import com.penfit.penfit.domain.financialprofile.entity.FinancialProfile;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.domain.pensionsetup.entity.PensionSetup;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest.FinancialProfilePayload;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeRequest.PensionSetupPayload;

public record PensionPlanGenerateRequest(
        FinancialProfilePayload financialProfile,
        PensionSetupPayload pensionSetup,
        PassportPayload passport
) {

    public record PassportPayload(
            String typeCode,
            String typeName,
            String typeSummary,
            Long sustainableMonthlyContribution,
            String biggestInterruptionRisk,
            MarketRiskPayload marketRiskLevel,
            String analysisSummary,
            String judgmentReason
    ) {
    }

    public record MarketRiskPayload(String code, String displayName) {
    }

    public static PensionPlanGenerateRequest of(FinancialProfile profile, PensionSetup setup,
                                                PensionPassport passport) {
        return new PensionPlanGenerateRequest(
                FinancialProfilePayload.from(profile),
                PensionSetupPayload.from(setup),
                new PassportPayload(
                        passport.getTypeCode().name(),
                        passport.getTypeCode().getDisplayName(),
                        passport.getTypeSummary(),
                        passport.getSustainableMonthlyContribution(),
                        passport.getBiggestInterruptionRiskCode().name(),
                        new MarketRiskPayload(
                                passport.getMarketRiskLevel().name(),
                                passport.getMarketRiskLevel().getDisplayName()),
                        passport.getSummary(),
                        passport.getJudgmentReason()));
    }
}
