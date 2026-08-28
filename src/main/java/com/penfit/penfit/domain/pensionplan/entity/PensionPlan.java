package com.penfit.penfit.domain.pensionplan.entity;

import com.penfit.penfit.global.common.BaseTimeEntity;
import com.penfit.penfit.global.enums.AccountType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "pension_plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionPlan extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "passport_id", nullable = false)
    private Long passportId;

    @Column(name = "plan_name", nullable = false, length = 50)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Column(name = "monthly_contribution", nullable = false)
    private Long monthlyContribution;

    @Column(name = "stock_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal stockRatio;

    @Column(name = "bond_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal bondRatio;

    @Column(name = "deposit_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal depositRatio;

    @Column(name = "recommendation_reason", nullable = false)
    private String recommendationReason;

    @Column(name = "expected_future_asset", nullable = false)
    private Long expectedFutureAsset;

    @Column(name = "contribution_years", nullable = false)
    private Integer contributionYears;

    @Column(name = "expected_return_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal expectedReturnRate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_raw_response", nullable = false)
    private String aiRawResponse;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Builder
    private PensionPlan(Long userId, Long passportId, String planName, AccountType accountType,
                        Long monthlyContribution, BigDecimal stockRatio, BigDecimal bondRatio,
                        BigDecimal depositRatio, String recommendationReason, Long expectedFutureAsset,
                        Integer contributionYears, BigDecimal expectedReturnRate,
                        String aiRawResponse, String modelVersion) {
        this.userId = userId;
        this.passportId = passportId;
        this.planName = planName;
        this.accountType = accountType;
        this.monthlyContribution = monthlyContribution;
        this.stockRatio = stockRatio;
        this.bondRatio = bondRatio;
        this.depositRatio = depositRatio;
        this.recommendationReason = recommendationReason;
        this.expectedFutureAsset = expectedFutureAsset;
        this.contributionYears = contributionYears;
        this.expectedReturnRate = expectedReturnRate;
        this.aiRawResponse = aiRawResponse;
        this.modelVersion = modelVersion;
    }
}
