package com.penfit.penfit.domain.rehearsal.dto;

import com.penfit.penfit.domain.rehearsal.entity.Rehearsal;
import com.penfit.penfit.domain.rehearsal.entity.RehearsalAnswer;
import com.penfit.penfit.global.common.CodeName;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record RehearsalDetailResponse(
        Long rehearsalId,
        CodeName status,
        Long previewFutureAsset,
        int answeredCount,
        int totalScenarios,
        boolean readyToComplete,
        Integer retryCount,
        String failureCode,
        String failureMessage,
        OffsetDateTime completedAt,
        List<AnswerResponse> answers
) {

    public record AnswerResponse(
            String scenarioCode,
            String optionCode,
            OffsetDateTime answeredAt
    ) {
    }

    public static RehearsalDetailResponse of(Rehearsal rehearsal, List<RehearsalAnswer> answers, int totalScenarios) {
        List<AnswerResponse> sorted = answers.stream()
                .sorted(Comparator.comparingInt(answer -> answer.getScenarioCode().getDisplayOrder()))
                .map(answer -> new AnswerResponse(
                        answer.getScenarioCode().name(),
                        answer.getOptionCode().name(),
                        answer.getAnsweredAt()))
                .toList();

        return new RehearsalDetailResponse(
                rehearsal.getId(),
                CodeName.of(rehearsal.getStatus(), rehearsal.getStatus().getDisplayName()),
                rehearsal.getPreviewFutureAsset(),
                sorted.size(),
                totalScenarios,
                sorted.size() == totalScenarios,
                rehearsal.getRetryCount(),
                rehearsal.getFailureCode(),
                rehearsal.getFailureMessage(),
                rehearsal.getCompletedAt(),
                sorted);
    }
}
