package com.penfit.penfit.domain.passport.dto;

import com.penfit.penfit.domain.passport.entity.PassportDetailedAnalysis;
import com.penfit.penfit.domain.passport.entity.PensionPassport;
import com.penfit.penfit.global.common.CodeName;

import java.time.OffsetDateTime;
import java.util.List;

public record PassportResponse(
        Long passportId,
        CodeName type,
        String typeDescription,
        Long sustainableMonthlyContribution,
        CodeName biggestInterruptionRisk,
        CodeName marketRiskLevel,
        String summary,
        String judgmentReason,
        List<DetailedAnalysisResponse> detailedAnalysis,
        OffsetDateTime createdAt
) {

    public record DetailedAnalysisResponse(
            CodeName scenario,
            String selectedOptionCode,
            int displayOrder,
            String behaviorSummary,
            String interpretation
    ) {
    }

    public static PassportResponse of(PensionPassport passport, List<PassportDetailedAnalysis> analyses) {
        return new PassportResponse(
                passport.getId(),
                CodeName.of(passport.getTypeCode(), passport.getTypeCode().getDisplayName()),
                passport.getTypeCode().getDescription(),
                passport.getSustainableMonthlyContribution(),
                CodeName.of(passport.getBiggestInterruptionRiskCode(),
                        passport.getBiggestInterruptionRiskCode().getDisplayName()),
                CodeName.of(passport.getMarketRiskLevel(), passport.getMarketRiskLevel().getDisplayName()),
                passport.getSummary(),
                passport.getJudgmentReason(),
                analyses.stream()
                        .map(analysis -> new DetailedAnalysisResponse(
                                CodeName.of(analysis.getScenarioCode(),
                                        analysis.getScenarioCode().getDisplayName()),
                                analysis.getSelectedOptionCode().name(),
                                analysis.getDisplayOrder(),
                                analysis.getBehaviorSummary(),
                                analysis.getInterpretation()))
                        .toList(),
                passport.getCreatedAt());
    }
}
