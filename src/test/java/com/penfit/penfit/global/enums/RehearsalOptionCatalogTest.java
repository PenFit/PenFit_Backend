package com.penfit.penfit.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RehearsalOptionCatalogTest {

    @Test
    @DisplayName("Enum 명세서에 정의된 허용 조합은 정확히 31개다")
    void totalCombinationsIs31() {
        assertThat(RehearsalOptionCatalog.totalCombinations()).isEqualTo(31);
    }

    @Test
    @DisplayName("시장 하락만 선택지가 6개이고 나머지 다섯 상황은 5개다")
    void marketDownturnHasSixOptions() {
        assertThat(RehearsalOptionCatalog.optionsOf(ScenarioCode.MARKET_DOWNTURN)).hasSize(6);

        for (ScenarioCode scenarioCode : ScenarioCode.values()) {
            if (scenarioCode != ScenarioCode.MARKET_DOWNTURN) {
                assertThat(RehearsalOptionCatalog.optionsOf(scenarioCode)).hasSize(5);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ScenarioCode.class)
    @DisplayName("모든 상황에 선택지가 등록되어 있고 노출 순서는 1부터 이어진다")
    void everyScenarioHasOrderedOptions(ScenarioCode scenarioCode) {
        List<RehearsalOptionCatalog.Entry> entries = RehearsalOptionCatalog.optionsOf(scenarioCode);

        assertThat(entries).isNotEmpty();
        for (int i = 0; i < entries.size(); i++) {
            assertThat(entries.get(i).displayOrder()).isEqualTo(i + 1);
            assertThat(entries.get(i).scenarioCode()).isEqualTo(scenarioCode);
            assertThat(entries.get(i).meaning()).isNotBlank();
        }
    }

    @Test
    @DisplayName("한 상황 안에서 같은 선택지 코드가 중복되지 않는다")
    void noDuplicateOptionWithinScenario() {
        for (ScenarioCode scenarioCode : ScenarioCode.values()) {
            List<OptionCode> codes = RehearsalOptionCatalog.optionsOf(scenarioCode).stream()
                    .map(RehearsalOptionCatalog.Entry::optionCode)
                    .toList();
            assertThat(codes).doesNotHaveDuplicates();
        }
    }

    @Test
    @DisplayName("고유 선택지 코드 19개가 모두 최소 한 번은 쓰인다")
    void everyOptionCodeIsUsed() {
        List<OptionCode> used = java.util.Arrays.stream(ScenarioCode.values())
                .flatMap(scenarioCode -> RehearsalOptionCatalog.optionsOf(scenarioCode).stream())
                .map(RehearsalOptionCatalog.Entry::optionCode)
                .distinct()
                .toList();

        assertThat(OptionCode.values()).hasSize(19);
        assertThat(used).containsExactlyInAnyOrder(OptionCode.values());
    }

    @Test
    @DisplayName("허용되지 않은 조합은 RH4001로 막는다")
    void rejectsCombinationOutsideCatalog() {
        // REBALANCE 는 시장 하락에만 있는 선택지다
        assertThat(RehearsalOptionCatalog.isAllowed(ScenarioCode.JOB_CHANGE, OptionCode.REBALANCE)).isFalse();

        assertThatThrownBy(() -> RehearsalOptionCatalog.require(ScenarioCode.JOB_CHANGE, OptionCode.REBALANCE))
                .isInstanceOf(com.penfit.penfit.global.error.BusinessException.class)
                .hasMessageContaining("JOB_CHANGE");
    }

    @Test
    @DisplayName("허용된 조합은 의미와 함께 조회된다")
    void acceptsCombinationInCatalog() {
        RehearsalOptionCatalog.Entry entry =
                RehearsalOptionCatalog.require(ScenarioCode.CHILDBIRTH, OptionCode.REDUCE_TO_MINIMUM);

        assertThat(entry.displayOrder()).isEqualTo(2);
        assertThat(entry.meaning()).isEqualTo("육아휴직 기간 최소 금액만 납입");
    }

    @Test
    @DisplayName("상품 종류와 계좌 종류 매핑은 명세서 조합만 허용한다")
    void productTypeMapsToSingleAccountType() {
        assertThat(ProductType.FUND_ACCOUNT.supports(AccountType.PENSION_SAVINGS_FUND)).isTrue();
        assertThat(ProductType.FUND_ACCOUNT.supports(AccountType.INDIVIDUAL_IRP)).isFalse();
        assertThat(ProductType.IRP_ACCOUNT.getAllowedAccountType()).isEqualTo(AccountType.INDIVIDUAL_IRP);
    }
}
