package com.penfit.penfit.domain.mission.dto;

import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.global.common.ServiceTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record MissionCompletionResponse(
        int year,
        int completedCount,
        long totalSavedAmount,
        long totalPensionImpactAmount,
        List<CompletionItem> completions
) {

    public record CompletionItem(
            Long missionId,
            String title,
            Long targetAmount,
            Long monthlyEquivalentAmount,
            Long pensionImpactAmount,
            LocalDate completedDate,
            OffsetDateTime completedAt
    ) {
    }

    public static MissionCompletionResponse of(int year, List<BehaviorMission> missions) {
        return new MissionCompletionResponse(
                year,
                missions.size(),
                missions.stream().mapToLong(BehaviorMission::getTargetAmount).sum(),
                missions.stream()
                        .mapToLong(mission -> mission.getPensionImpactAmount() == null
                                ? 0L : mission.getPensionImpactAmount())
                        .sum(),
                missions.stream()
                        .map(mission -> new CompletionItem(
                                mission.getId(),
                                mission.getTitle(),
                                mission.getTargetAmount(),
                                mission.monthlyEquivalentAmount(),
                                mission.getPensionImpactAmount(),
                                ServiceTime.toLocalDate(mission.getCompletedAt()),
                                mission.getCompletedAt()))
                        .toList());
    }
}
