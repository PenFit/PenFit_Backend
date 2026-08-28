package com.penfit.penfit.domain.rehearsal.dto;

import com.penfit.penfit.domain.rehearsal.entity.RehearsalScenario.ContextCard;

import java.util.List;

public record ScenarioResponse(
        String scenarioCode,
        int displayOrder,
        String title,
        String badge,
        String situation,
        String question,
        Long baselineContribution,
        List<ContextCard> contextCards,
        String notice,
        List<OptionResponse> options
) {

    public record OptionResponse(
            String optionCode,
            int displayOrder,
            String label,
            String title,
            String description
    ) {
    }
}
