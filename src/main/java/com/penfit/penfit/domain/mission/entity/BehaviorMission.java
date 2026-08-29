package com.penfit.penfit.domain.mission.entity;

import com.penfit.penfit.global.common.BaseTimeEntity;
import com.penfit.penfit.global.enums.MissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "behavior_missions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BehaviorMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spending_analysis_id", nullable = false)
    private Long spendingAnalysisId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String reason;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "pension_impact_amount")
    private Long pensionImpactAmount;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Builder
    private BehaviorMission(Long userId, Long spendingAnalysisId, String title, String description,
                            String reason, Long targetAmount, Integer durationDays, LocalDate dueDate,
                            String modelVersion) {
        this.userId = userId;
        this.spendingAnalysisId = spendingAnalysisId;
        this.title = title;
        this.description = description;
        this.reason = reason;
        this.targetAmount = targetAmount;
        this.durationDays = durationDays;
        this.dueDate = dueDate;
        this.modelVersion = modelVersion;
        this.status = MissionStatus.PENDING;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isCompleted() {
        return this.status == MissionStatus.COMPLETED;
    }

    public boolean isExpired(LocalDate today) {
        return !isCompleted() && today.isAfter(dueDate);
    }

    public void start() {
        if (this.status != MissionStatus.PENDING) {
            throw new IllegalStateException("시작 전 상태에서만 시작할 수 있습니다.");
        }
        this.status = MissionStatus.IN_PROGRESS;
        this.startedAt = OffsetDateTime.now();
    }

    public void complete(long pensionImpactAmount) {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
        this.pensionImpactAmount = pensionImpactAmount;
    }
}
