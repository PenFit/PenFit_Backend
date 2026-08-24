package com.penfit.penfit.global.enums;

import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.penfit.penfit.global.enums.OptionCode.*;
import static com.penfit.penfit.global.enums.ScenarioCode.*;

public final class RehearsalOptionCatalog {

    public record Entry(ScenarioCode scenarioCode, OptionCode optionCode, int displayOrder, String meaning) {
    }

    private static final Map<ScenarioCode, List<Entry>> BY_SCENARIO = new LinkedHashMap<>();

    static {
        register(JOB_CHANGE,
                KEEP, "비상금을 사용해 기존 납입 유지",
                REDUCE_HALF, "월 납입액을 절반으로 감액",
                PAUSE_UNTIL_REEMPLOYED, "재취업할 때까지 납입 중단",
                STOP_AND_REPLAN, "즉시 중단하고 취업 후 계획 재수립",
                CLOSE_ACCOUNT, "계좌를 해지해 생활비로 사용");

        register(INDEPENDENCE,
                CUT_EXPENSE_AND_KEEP, "생활비를 줄여 기존 납입 유지",
                REDUCE_HALF, "월 납입액을 절반으로 감액",
                PAUSE_TEMPORARILY, "독립 비용 마련까지 일시 중단",
                DELAY_EVENT, "독립 시기를 미루고 납입 유지",
                CLOSE_ACCOUNT, "계좌를 해지해 보증금으로 사용");

        register(MARRIAGE,
                REDUCE_EVENT_COST_AND_KEEP, "결혼 비용을 줄이고 납입 유지",
                REDUCE_CONTRIBUTION, "납입액을 줄여 결혼자금 확보",
                PAUSE_UNTIL_EVENT, "결혼할 때까지 납입 중단",
                DELAY_EVENT, "결혼 시기를 미루고 납입 유지",
                CLOSE_ACCOUNT, "계좌를 해지해 결혼자금으로 사용");

        register(HOME_PURCHASE,
                DELAY_EVENT, "주택 구매를 미루고 납입 유지",
                REDUCE_CONTRIBUTION, "납입액을 줄여 주택자금 확보",
                PAUSE_UNTIL_EVENT, "주택자금 마련까지 납입 중단",
                CHOOSE_ALTERNATIVE_AND_KEEP, "더 저렴한 주택을 선택하고 납입 유지",
                CLOSE_ACCOUNT, "계좌를 해지해 주택자금으로 사용");

        register(CHILDBIRTH,
                KEEP, "기존 납입액 유지",
                REDUCE_TO_MINIMUM, "육아휴직 기간 최소 금액만 납입",
                PAUSE_SIX_MONTHS, "6개월 중단 후 복직 시 재개",
                CUT_EXPENSE_AND_KEEP, "다른 생활비를 줄여 납입 유지",
                CLOSE_ACCOUNT, "계좌를 해지해 출산·육아비로 사용");

        register(MARKET_DOWNTURN,
                KEEP, "장기 계획과 기존 납입 유지",
                REBALANCE, "원래 계획에 맞게 자산 비중 조정",
                INCREASE_SAFE_ASSET, "안전자산 비중 확대",
                PAUSE_CONTRIBUTION, "추가 하락 우려로 납입 중단",
                SELL_OR_CLOSE, "투자상품 정리 또는 계좌 해지",
                INCREASE_CONTRIBUTION, "하락 시점에 납입액 확대");
    }

    private RehearsalOptionCatalog() {
    }

    public static List<Entry> optionsOf(ScenarioCode scenarioCode) {
        return BY_SCENARIO.get(scenarioCode);
    }

    public static boolean isAllowed(ScenarioCode scenarioCode, OptionCode optionCode) {
        List<Entry> entries = BY_SCENARIO.get(scenarioCode);
        if (entries == null) {
            return false;
        }
        return entries.stream().anyMatch(entry -> entry.optionCode() == optionCode);
    }

    public static Entry require(ScenarioCode scenarioCode, OptionCode optionCode) {
        List<Entry> entries = BY_SCENARIO.get(scenarioCode);
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry.optionCode() == optionCode) {
                    return entry;
                }
            }
        }
        throw new BusinessException(ErrorCode.INVALID_SCENARIO_OPTION,
                "%s 상황에서는 %s 선택지를 사용할 수 없습니다.".formatted(scenarioCode, optionCode));
    }

    public static int totalCombinations() {
        return BY_SCENARIO.values().stream().mapToInt(List::size).sum();
    }

    private static void register(ScenarioCode scenarioCode, Object... optionAndMeaning) {
        List<Entry> entries = new java.util.ArrayList<>();
        for (int i = 0; i < optionAndMeaning.length; i += 2) {
            entries.add(new Entry(
                    scenarioCode,
                    (OptionCode) optionAndMeaning[i],
                    (i / 2) + 1,
                    (String) optionAndMeaning[i + 1]));
        }
        BY_SCENARIO.put(scenarioCode, List.copyOf(entries));
    }
}
