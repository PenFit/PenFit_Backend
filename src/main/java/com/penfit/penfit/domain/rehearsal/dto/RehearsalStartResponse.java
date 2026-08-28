package com.penfit.penfit.domain.rehearsal.dto;

import com.penfit.penfit.domain.rehearsal.entity.Rehearsal;
import com.penfit.penfit.global.common.CodeName;

public record RehearsalStartResponse(
        Long rehearsalId,
        CodeName status,
        Long previewFutureAsset,
        int totalScenarios
) {

    public static RehearsalStartResponse of(Rehearsal rehearsal, int totalScenarios) {
        return new RehearsalStartResponse(
                rehearsal.getId(),
                CodeName.of(rehearsal.getStatus(), rehearsal.getStatus().getDisplayName()),
                rehearsal.getPreviewFutureAsset(),
                totalScenarios);
    }
}
