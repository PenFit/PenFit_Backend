package com.penfit.penfit.domain.passport.entity;

import com.penfit.penfit.global.enums.MarketRiskLevel;
import com.penfit.penfit.global.enums.PassportTypeCode;
import com.penfit.penfit.global.enums.ScenarioCode;
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

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "pension_passports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionPassport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "rehearsal_id", nullable = false)
    private Long rehearsalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_code", nullable = false, length = 40)
    private PassportTypeCode typeCode;

    @Column(name = "sustainable_monthly_contribution", nullable = false)
    private Long sustainableMonthlyContribution;

    @Enumerated(EnumType.STRING)
    @Column(name = "biggest_interruption_risk_code", nullable = false, length = 30)
    private ScenarioCode biggestInterruptionRiskCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_risk_level", nullable = false, length = 10)
    private MarketRiskLevel marketRiskLevel;

    @Column(name = "type_summary")
    private String typeSummary;

    @Column(nullable = false)
    private String summary;

    @Column(name = "judgment_reason", nullable = false)
    private String judgmentReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_raw_response", nullable = false)
    private String aiRawResponse;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Builder
    private PensionPassport(Long userId, Long rehearsalId, PassportTypeCode typeCode,
                            Long sustainableMonthlyContribution, ScenarioCode biggestInterruptionRiskCode,
                            MarketRiskLevel marketRiskLevel, String typeSummary, String summary,
                            String judgmentReason, String aiRawResponse, String modelVersion) {
        this.userId = userId;
        this.rehearsalId = rehearsalId;
        this.typeCode = typeCode;
        this.sustainableMonthlyContribution = sustainableMonthlyContribution;
        this.biggestInterruptionRiskCode = biggestInterruptionRiskCode;
        this.marketRiskLevel = marketRiskLevel;
        this.typeSummary = typeSummary;
        this.summary = summary;
        this.judgmentReason = judgmentReason;
        this.aiRawResponse = aiRawResponse;
        this.modelVersion = modelVersion;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
