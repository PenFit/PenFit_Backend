package com.penfit.penfit.domain.financialprofile.entity;

import com.penfit.penfit.global.common.BaseTimeEntity;
import com.penfit.penfit.global.enums.AgeBand;
import com.penfit.penfit.global.enums.AssetBand;
import com.penfit.penfit.global.enums.DebtBand;
import com.penfit.penfit.global.enums.EmergencyFundBand;
import com.penfit.penfit.global.enums.LivingExpenseBand;
import com.penfit.penfit.global.enums.OccupationType;
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

@Getter
@Entity
@Table(name = "financial_profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_band", nullable = false, length = 20)
    private AgeBand ageBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupation_type", nullable = false, length = 30)
    private OccupationType occupationType;

    @Column(name = "monthly_salary", nullable = false)
    private Long monthlySalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "living_expense_band", nullable = false, length = 30)
    private LivingExpenseBand livingExpenseBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_band", nullable = false, length = 30)
    private AssetBand assetBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "debt_band", nullable = false, length = 30)
    private DebtBand debtBand;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_fund_band", nullable = false, length = 30)
    private EmergencyFundBand emergencyFundBand;

    @Column(name = "monthly_savings", nullable = false)
    private Long monthlySavings;

    @Column(name = "current_investment", nullable = false)
    private Long currentInvestment;

    @Builder
    private FinancialProfile(Long userId, AgeBand ageBand, OccupationType occupationType, Long monthlySalary,
                             LivingExpenseBand livingExpenseBand, AssetBand assetBand, DebtBand debtBand,
                             EmergencyFundBand emergencyFundBand, Long monthlySavings, Long currentInvestment) {
        this.userId = userId;
        this.ageBand = ageBand;
        this.occupationType = occupationType;
        this.monthlySalary = monthlySalary;
        this.livingExpenseBand = livingExpenseBand;
        this.assetBand = assetBand;
        this.debtBand = debtBand;
        this.emergencyFundBand = emergencyFundBand;
        this.monthlySavings = monthlySavings;
        this.currentInvestment = currentInvestment;
    }
}
