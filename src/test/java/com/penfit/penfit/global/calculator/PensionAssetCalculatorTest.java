package com.penfit.penfit.global.calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PensionAssetCalculatorTest {

    private static final BigDecimal RATE_5_PERCENT = new BigDecimal("0.05");

    private final PensionAssetCalculator calculator = new PensionAssetCalculator();

    @ParameterizedTest(name = "월 {0}원을 30년 납입하면 {1}원")
    @CsvSource({
            "100000, 83225864",
            "120000, 99871036",
            "30000,  24967759"
    })
    @DisplayName("디자인에 표기된 예상 연금자산과 일치한다")
    void matchesDesignFigures(long monthly, long expected) {
        assertThat(calculator.futureValue(monthly, RATE_5_PERCENT, 30)).isEqualTo(expected);
    }

    @Test
    @DisplayName("납입액에 비례한다")
    void scalesLinearlyWithContribution() {
        long single = calculator.futureValue(100_000, RATE_5_PERCENT, 30);
        long triple = calculator.futureValue(300_000, RATE_5_PERCENT, 30);

        assertThat(triple).isCloseTo(single * 3, org.assertj.core.data.Offset.offset(10L));
    }

    @Test
    @DisplayName("납입 기간이 길수록 커진다")
    void growsWithYears() {
        long tenYears = calculator.futureValue(100_000, RATE_5_PERCENT, 10);
        long twentyYears = calculator.futureValue(100_000, RATE_5_PERCENT, 20);
        long thirtyYears = calculator.futureValue(100_000, RATE_5_PERCENT, 30);

        assertThat(tenYears).isLessThan(twentyYears);
        assertThat(twentyYears).isLessThan(thirtyYears);
    }

    @Test
    @DisplayName("수익률이 0이면 납입 원금과 같다")
    void returnsPrincipalWhenRateIsZero() {
        assertThat(calculator.futureValue(100_000, BigDecimal.ZERO, 30))
                .isEqualTo(100_000L * 12 * 30);
    }

    @Test
    @DisplayName("수익이 붙으므로 언제나 납입 원금보다 크다")
    void alwaysExceedsPrincipal() {
        long principal = 100_000L * 12 * 30;

        assertThat(calculator.futureValue(100_000, RATE_5_PERCENT, 30)).isGreaterThan(principal);
    }

    @Test
    @DisplayName("납입액이 0이면 결과도 0이다")
    void returnsZeroForZeroContribution() {
        assertThat(calculator.futureValue(0, RATE_5_PERCENT, 30)).isZero();
    }

    @Test
    @DisplayName("잘못된 입력은 거부한다")
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> calculator.futureValue(-1, RATE_5_PERCENT, 30))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.futureValue(100_000, RATE_5_PERCENT, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.futureValue(100_000, new BigDecimal("-0.01"), 30))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
