package com.penfit.penfit.domain.mission.dto;

import com.penfit.penfit.domain.mission.entity.BehaviorMission;
import com.penfit.penfit.global.common.CodeName;
import com.penfit.penfit.global.enums.CategoryCode;
import com.penfit.penfit.global.enums.MissionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public record BehaviorMissionResponse(
        Long missionId,
        String title,
        String description,
        String reason,
        Long targetAmount,
        Long monthlyEquivalentAmount,
        Integer durationDays,
        LocalDate dueDate,
        long daysLeft,
        CodeName status,
        CodeName topCategory,
        BigDecimal topCategoryRatio,
        Long pensionImpactAmount,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt
) {

    public static BehaviorMissionResponse of(BehaviorMission mission, CategoryCode topCategory,
                                             BigDecimal topCategoryRatio, LocalDate today) {
        MissionStatus status = mission.isExpired(today) ? MissionStatus.EXPIRED : mission.getStatus();

        return new BehaviorMissionResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getReason(),
                mission.getTargetAmount(),
                mission.monthlyEquivalentAmount(),
                mission.getDurationDays(),
                mission.getDueDate(),
                Math.max(ChronoUnit.DAYS.between(today, mission.getDueDate()), 0),
                CodeName.of(status, status.getDisplayName()),
                CodeName.of(topCategory, topCategory.getDisplayName()),
                topCategoryRatio,
                mission.getPensionImpactAmount(),
                mission.getStartedAt(),
                mission.getCompletedAt());
    }
}
