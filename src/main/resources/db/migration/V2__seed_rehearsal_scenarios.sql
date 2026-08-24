-- 리허설 시나리오 6개와 허용 선택지 31개 (Enum 단일 기준 명세서와 1:1 대응)
-- 시나리오 지문의 금액은 사용자 설정액으로 치환하지 않고 원문 그대로 고정한다.

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('JOB_CHANGE', 1, '이직', '현재 29세 · 퇴사 후 1개월차',
     '새로운 직장을 찾기 위해 퇴사했습니다. 앞으로 4개월 동안 소득이 없고, 현재 비상금으로 약 6개월의 생활비를 감당할 수 있습니다. 현재 연금에는 매월 10만 원을 납입하고 있습니다.',
     '소득이 없는 동안 연금계획을 어떻게 하시겠습니까?',
     100000, '[{"label": "현재 월 납입액", "value": "10만원"}, {"label": "비상금 현황", "value": "6개월 생활비"}]'::jsonb, NULL);

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('JOB_CHANGE', 'KEEP', 1, '비상금을 사용해 월 10만 원을 그대로 납입한다', '복리 효과를 유지하되 비상금이 2개월 줄어듦'),
    ('JOB_CHANGE', 'REDUCE_HALF', 2, '월 납입액을 10만 원에서 5만 원으로 줄인다', '부담을 절반으로 줄이고 비상금을 아낌'),
    ('JOB_CHANGE', 'PAUSE_UNTIL_REEMPLOYED', 3, '취업할 때까지 납입을 잠시 중단한다', '약 4개월간 중단 후 취업 시 재개'),
    ('JOB_CHANGE', 'STOP_AND_REPLAN', 4, '이번 달부터 납입을 중단하고 취업 후 다시 계획한다', '단기 부담 제거, 재취업 후 신규 계획'),
    ('JOB_CHANGE', 'CLOSE_ACCOUNT', 5, '연금계좌를 해지하고 쌓인 돈을 생활비로 사용한다', '세액공제 혜택 상실 및 장기 손실 우려');

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('INDEPENDENCE', 2, '독립', '독립 준비 · 보증금 마련 시점',
     '부모님과 함께 살다가 독립하게 되었습니다. 보증금 1,000만 원이 필요하고, 월 생활비가 50만 원 증가합니다. 현재 연금에는 매월 10만 원을 납입하고 있습니다.',
     '독립 후 연금 납입을 어떻게 조정하시겠습니까?',
     100000, '[{"label": "현재 월 납입액", "value": "10만원"}, {"label": "생활비 증가", "value": "월 50만원"}]'::jsonb, NULL);

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('INDEPENDENCE', 'CUT_EXPENSE_AND_KEEP', 1, '생활비를 줄이고 기존 월 10만 원 납입을 유지한다', '생활비를 줄여 기존 납입 유지'),
    ('INDEPENDENCE', 'REDUCE_HALF', 2, '월 납입액을 10만 원에서 5만 원으로 줄인다', '월 납입액을 절반으로 감액'),
    ('INDEPENDENCE', 'PAUSE_TEMPORARILY', 3, '이사 비용을 마련할 때까지만 납입을 중단한다', '독립 비용 마련까지 일시 중단'),
    ('INDEPENDENCE', 'DELAY_EVENT', 4, '독립 시기를 늦추고 연금 납입을 유지한다', '독립 시기를 미루고 납입 유지'),
    ('INDEPENDENCE', 'CLOSE_ACCOUNT', 5, '연금계좌를 해지하고 보증금에 사용한다', '계좌를 해지해 보증금으로 사용');

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('MARRIAGE', 3, '결혼', '2년 뒤 결혼 예정',
     '2년 뒤 결혼을 계획하고 있습니다. 예상 결혼비용 중 본인이 준비해야 하는 금액은 3,000만 원입니다. 현재 저축만으로는 약 1,000만 원이 부족합니다.',
     '부족한 결혼자금을 마련하기 위해 연금계획을 어떻게 변경하시겠습니까?',
     NULL, '[{"label": "본인 준비 금액", "value": "3,000만원"}, {"label": "부족한 금액", "value": "1,000만원"}]'::jsonb, NULL);

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('MARRIAGE', 'REDUCE_EVENT_COST_AND_KEEP', 1, '연금 납입은 유지하고 결혼 규모나 비용을 줄인다', '결혼 비용을 줄이고 납입 유지'),
    ('MARRIAGE', 'REDUCE_CONTRIBUTION', 2, '월 연금 납입액을 줄이고 차액을 결혼자금으로 모은다', '납입액을 줄여 결혼자금 확보'),
    ('MARRIAGE', 'PAUSE_UNTIL_EVENT', 3, '결혼할 때까지 연금 납입을 잠시 중단한다', '결혼할 때까지 납입 중단'),
    ('MARRIAGE', 'DELAY_EVENT', 4, '결혼 시기를 늦추고 현재 연금계획을 유지한다', '결혼 시기를 미루고 납입 유지'),
    ('MARRIAGE', 'CLOSE_ACCOUNT', 5, '연금계좌를 해지해 부족한 결혼자금을 마련한다', '계좌를 해지해 결혼자금으로 사용');

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('HOME_PURCHASE', 4, '주택 구매', '주택 구매 자금 2,000만원 부족',
     '원하는 주택을 구입하려면 본인 자금 8,000만 원이 필요합니다. 현재 준비한 금액은 6,000만 원으로, 2,000만 원이 부족합니다. 현재 계획대로라면 약 3년 뒤 부족한 금액을 모을 수 있습니다.',
     '주택 구매와 연금계획을 어떻게 조정하시겠습니까?',
     NULL, '[{"label": "필요 자금", "value": "8,000만원"}, {"label": "부족한 금액", "value": "2,000만원"}]'::jsonb, 'IRP는 필요할 때 자유롭게 일부 금액을 꺼낼 수 있는 계좌가 아닙니다. 중도인출은 법에서 정한 사유와 요건을 충족하는 경우에만 가능하며, 계좌 해지 시 세금상 불이익이 발생할 수 있습니다.');

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('HOME_PURCHASE', 'DELAY_EVENT', 1, '주택 구매를 3년 미루고 연금 납입을 유지한다', '주택 구매를 미루고 납입 유지'),
    ('HOME_PURCHASE', 'REDUCE_CONTRIBUTION', 2, '월 연금 납입액을 줄이고 주택자금을 더 많이 모은다', '납입액을 줄여 주택자금 확보'),
    ('HOME_PURCHASE', 'PAUSE_UNTIL_EVENT', 3, '주택자금이 마련될 때까지 연금 납입을 중단한다', '주택자금 마련까지 납입 중단'),
    ('HOME_PURCHASE', 'CHOOSE_ALTERNATIVE_AND_KEEP', 4, '더 저렴한 주택을 선택하고 연금 납입을 유지한다', '더 저렴한 주택을 선택하고 납입 유지'),
    ('HOME_PURCHASE', 'CLOSE_ACCOUNT', 5, '연금계좌를 해지해 주택 구매자금으로 사용한다', '계좌를 해지해 주택자금으로 사용');

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('CHILDBIRTH', 5, '출산', '육아휴직 6개월 예정',
     '자녀가 태어나면서 매월 생활비가 60만 원 증가했습니다. 육아휴직으로 앞으로 6개월 동안 소득도 줄어듭니다. 현재 연금에는 매월 15만 원을 납입하고 있습니다.',
     '육아휴직 기간 동안 연금 납입을 어떻게 하시겠습니까?',
     150000, '[{"label": "현재 월 납입액", "value": "15만원"}, {"label": "생활비 증가", "value": "월 60만원"}]'::jsonb, NULL);

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('CHILDBIRTH', 'KEEP', 1, '기존 월 15만 원 납입을 그대로 유지한다', '기존 납입액 유지'),
    ('CHILDBIRTH', 'REDUCE_TO_MINIMUM', 2, '육아휴직 기간에는 월 5만 원만 납입한다', '육아휴직 기간 최소 금액만 납입'),
    ('CHILDBIRTH', 'PAUSE_SIX_MONTHS', 3, '6개월 동안 납입을 중단하고 복직 후 다시 시작한다', '6개월 중단 후 복직 시 재개'),
    ('CHILDBIRTH', 'CUT_EXPENSE_AND_KEEP', 4, '다른 생활비를 줄여 연금 납입을 유지한다', '다른 생활비를 줄여 납입 유지'),
    ('CHILDBIRTH', 'CLOSE_ACCOUNT', 5, '연금계좌를 해지해 출산·육아비에 사용한다', '계좌를 해지해 출산·육아비로 사용');

INSERT INTO rehearsal_scenarios (scenario_code, display_order, title, badge, situation, question, baseline_contribution, context_cards, irp_notice) VALUES
    ('MARKET_DOWNTURN', 6, '시장 하락', '평가금액 20% 하락',
     '금융시장 하락으로 연금계좌의 평가금액이 20% 감소했습니다. 지금까지 납입한 원금은 1,500만 원이지만 현재 평가금액은 1,200만 원입니다.',
     '연금계좌를 어떻게 운용하시겠습니까?',
     NULL, '[{"label": "납입 원금", "value": "1,500만원"}, {"label": "현재 평가금액", "value": "1,200만원"}]'::jsonb, NULL);

INSERT INTO rehearsal_scenario_options (scenario_code, option_code, display_order, title, description) VALUES
    ('MARKET_DOWNTURN', 'KEEP', 1, '장기투자 계획을 유지하고 기존 금액을 계속 납입한다', '장기 계획과 기존 납입 유지'),
    ('MARKET_DOWNTURN', 'REBALANCE', 2, '현재 자산 비중을 확인하고 원래 계획에 맞게 조정한다', '원래 계획에 맞게 자산 비중 조정'),
    ('MARKET_DOWNTURN', 'INCREASE_SAFE_ASSET', 3, '투자상품 비중을 줄이고 안전자산 비중을 높인다', '안전자산 비중 확대'),
    ('MARKET_DOWNTURN', 'PAUSE_CONTRIBUTION', 4, '추가 하락이 걱정되어 연금 납입을 중단한다', '추가 하락 우려로 납입 중단'),
    ('MARKET_DOWNTURN', 'SELL_OR_CLOSE', 5, '투자상품을 정리하거나 연금계좌를 해지한다', '투자상품 정리 또는 계좌 해지'),
    ('MARKET_DOWNTURN', 'INCREASE_CONTRIBUTION', 6, '가격이 하락한 시점이라고 판단해 납입액을 늘린다', '하락 시점에 납입액 확대');
