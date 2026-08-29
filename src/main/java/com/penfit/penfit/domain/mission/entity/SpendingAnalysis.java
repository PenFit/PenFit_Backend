package com.penfit.penfit.domain.mission.entity;

import com.penfit.penfit.global.enums.CategoryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "spending_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpendingAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "analysis_start_date", nullable = false)
    private LocalDate analysisStartDate;

    @Column(name = "analysis_end_date", nullable = false)
    private LocalDate analysisEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "top_category_code", nullable = false, length = 30)
    private CategoryCode topCategoryCode;

    @Column(name = "recurring_expense", nullable = false)
    private Long recurringExpense;

    @Column(name = "reducible_amount", nullable = false)
    private Long reducibleAmount;

    @Column(nullable = false)
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_raw_response", nullable = false)
    private String aiRawResponse;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Builder
    private SpendingAnalysis(Long userId, LocalDate analysisStartDate, LocalDate analysisEndDate,
                             CategoryCode topCategoryCode, Long recurringExpense, Long reducibleAmount,
                             String summary, String aiRawResponse, String modelVersion) {
        this.userId = userId;
        this.analysisStartDate = analysisStartDate;
        this.analysisEndDate = analysisEndDate;
        this.topCategoryCode = topCategoryCode;
        this.recurringExpense = recurringExpense;
        this.reducibleAmount = reducibleAmount;
        this.summary = summary;
        this.aiRawResponse = aiRawResponse;
        this.modelVersion = modelVersion;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
