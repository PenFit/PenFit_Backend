package com.penfit.penfit.global.client.ai.dto;

public record PassportAnalyzeResponse(
        String typeCode,
        String typeName,
        String typeSummary,
        Long sustainableMonthlyContribution,
        InterruptionRisk biggestInterruptionRisk,
        MarketRisk marketRiskLevel,
        String analysisSummary,
        String detailedAnalysisReport,
        String judgmentReason,
        String modelVersion
) {

    public record InterruptionRisk(String scenarioCode, String displayName) {
    }

    public record MarketRisk(String code, String displayName) {
    }
}
