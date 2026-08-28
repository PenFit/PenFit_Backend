package com.penfit.penfit.domain.rehearsal.entity;

import com.penfit.penfit.global.common.BaseTimeEntity;
import com.penfit.penfit.global.enums.RehearsalStatus;
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

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "rehearsals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rehearsal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RehearsalStatus status;

    @Column(name = "preview_future_asset", nullable = false)
    private Long previewFutureAsset;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "failure_code", length = 40)
    private String failureCode;

    @Column(name = "failure_message")
    private String failureMessage;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Builder
    private Rehearsal(Long userId, Long previewFutureAsset) {
        this.userId = userId;
        this.previewFutureAsset = previewFutureAsset;
        this.status = RehearsalStatus.IN_PROGRESS;
        this.retryCount = 0;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isInProgress() {
        return this.status == RehearsalStatus.IN_PROGRESS;
    }

    public boolean isFailed() {
        return this.status == RehearsalStatus.FAILED;
    }

    public boolean isAnalyzing() {
        return this.status == RehearsalStatus.ANALYZING;
    }

    public void startAnalysis() {
        this.status = RehearsalStatus.ANALYZING;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void retryAnalysis() {
        this.retryCount += 1;
        startAnalysis();
    }

    public void completeAnalysis() {
        this.status = RehearsalStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void failAnalysis(String failureCode, String failureMessage) {
        this.status = RehearsalStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
    }
}
