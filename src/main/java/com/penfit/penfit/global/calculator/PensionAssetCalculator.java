package com.penfit.penfit.global.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class PensionAssetCalculator {

    private static final int MONTHS_PER_YEAR = 12;
    private static final MathContext CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

    public long futureValue(long monthlyContribution, BigDecimal annualReturnRate, int years) {
        if (monthlyContribution < 0) {
            throw new IllegalArgumentException("월 납입액은 0원 이상이어야 합니다.");
        }
        if (years <= 0) {
            throw new IllegalArgumentException("납입 연수는 1년 이상이어야 합니다.");
        }
        if (annualReturnRate == null || annualReturnRate.signum() < 0) {
            throw new IllegalArgumentException("기대 수익률은 0 이상이어야 합니다.");
        }

        int months = years * MONTHS_PER_YEAR;
        BigDecimal contribution = BigDecimal.valueOf(monthlyContribution);

        if (annualReturnRate.signum() == 0) {
            return contribution.multiply(BigDecimal.valueOf(months)).longValueExact();
        }

        BigDecimal monthlyRate = annualReturnRate.divide(BigDecimal.valueOf(MONTHS_PER_YEAR), CONTEXT);
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(months, CONTEXT);
        BigDecimal factor = growth.subtract(BigDecimal.ONE).divide(monthlyRate, CONTEXT);

        return contribution.multiply(factor, CONTEXT).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
