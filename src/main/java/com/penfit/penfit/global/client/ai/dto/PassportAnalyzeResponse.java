package com.penfit.penfit.global.client.ai.dto;

import java.util.List;

public record PassportAnalyzeResponse(
        String typeCode,
        String typeName,
        String typeSummary,
        Long sustainableMonthlyContribution,
        InterruptionRisk biggestInterruptionRisk,
        MarketRisk marketRiskLevel,
        String analysisSummary,
        String judgmentReason,
        List<DetailedAnalysis> detailedAnalysis,
        String modelVersion
) {

    public record InterruptionRisk(String scenarioCode, String displayName) {
    }

    public record MarketRisk(String code, String displayName) {
    }

    public record DetailedAnalysis(
            String scenarioCode,
            String scenarioName,
            String selectedOptionCode,
            String behaviorSummary,
            String interpretation
    ) {
    }
}
