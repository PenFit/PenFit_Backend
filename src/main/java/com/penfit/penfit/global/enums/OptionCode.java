package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리허설 선택지 고유 코드 19개. 시나리오 공통 행동이면 같은 코드를 재사용하므로
 * 코드 하나만으로는 의미가 확정되지 않는다. 허용 조합과 상황별 의미는
 * {@link com.penfit.penfit.global.enums.RehearsalOptionCatalog} 에서 관리한다.
 */
public enum OptionCode {

    KEEP,
    REDUCE_HALF,
    PAUSE_UNTIL_REEMPLOYED,
    STOP_AND_REPLAN,
    CLOSE_ACCOUNT,
    CUT_EXPENSE_AND_KEEP,
    PAUSE_TEMPORARILY,
    DELAY_EVENT,
    REDUCE_EVENT_COST_AND_KEEP,
    REDUCE_CONTRIBUTION,
    PAUSE_UNTIL_EVENT,
    CHOOSE_ALTERNATIVE_AND_KEEP,
    REDUCE_TO_MINIMUM,
    PAUSE_SIX_MONTHS,
    REBALANCE,
    INCREASE_SAFE_ASSET,
    PAUSE_CONTRIBUTION,
    SELL_OR_CLOSE,
    INCREASE_CONTRIBUTION
}
