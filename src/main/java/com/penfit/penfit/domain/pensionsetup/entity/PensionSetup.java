package com.penfit.penfit.domain.pensionsetup.entity;

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

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "pension_setups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PensionSetup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Column(name = "monthly_contribution", nullable = false)
    private Long monthlyContribution;

    @Column(name = "preview_future_asset", nullable = false)
    private Long previewFutureAsset;

    @Column(name = "expected_return_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal expectedReturnRate;

    @Column(name = "contribution_years", nullable = false)
    private Integer contributionYears;

    @Builder
    private PensionSetup(Long userId, AccountType accountType, Long monthlyContribution,
                         Long previewFutureAsset, BigDecimal expectedReturnRate, Integer contributionYears) {
        this.userId = userId;
        this.accountType = accountType;
        this.monthlyContribution = monthlyContribution;
        this.previewFutureAsset = previewFutureAsset;
        this.expectedReturnRate = expectedReturnRate;
        this.contributionYears = contributionYears;
    }
}
