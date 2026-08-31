package com.penfit.penfit.global.client.ai.dto;

public record PassportAnalyzeResponse(
        String typeCode,
        String typeName,
        Long sustainableMonthlyContribution,
        String biggestInterruptionRisk,
        MarketRisk marketRiskLevel,
        String analysisSummary,
        String detailedAnalysisReport,
        String judgmentReason,
        String modelVersion
) {

    public record MarketRisk(String code, String displayName) {
    }
}
