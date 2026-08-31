package com.penfit.penfit.domain.passport.dto;

import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.global.common.CodeName;

import java.time.OffsetDateTime;

public record PassportResponse(
        Long passportId,
        CodeName type,
        String typeSummary,
        Long sustainableMonthlyContribution,
        CodeName biggestInterruptionRisk,
        CodeName marketRiskLevel,
        String summary,
        String judgmentReason,
        String detailedAnalysisReport,
        OffsetDateTime createdAt
) {

    public static PassportResponse of(PensionPassport passport) {
        return new PassportResponse(
                passport.getId(),
                CodeName.of(passport.getTypeCode(), passport.getTypeCode().getDisplayName()),
                passport.getTypeSummary(),
                passport.getSustainableMonthlyContribution(),
                CodeName.of(passport.getBiggestInterruptionRiskCode(),
                        passport.getBiggestInterruptionRiskCode().getDisplayName()),
                CodeName.of(passport.getMarketRiskLevel(), passport.getMarketRiskLevel().getDisplayName()),
                passport.getSummary(),
                passport.getJudgmentReason(),
                passport.getDetailedAnalysisReport(),
                passport.getCreatedAt());
    }
}
