INSERT INTO pension_products (provider_type, provider_name, product_name, account_type, product_type, summary, fee_min_rate, fee_max_rate, investment_scope, official_url, data_source, reference_date) VALUES
    ('BANK', '하나은행', '하나은행 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', '예금과 펀드를 함께 담아 안정적으로 굴리기 좋은 은행 IRP', 0.0000, 0.0034, '예금 + 펀드 380종', 'https://www.kebhana.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', 'KB증권', 'KB증권 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', '증권사 라인업에 은행 계열 상담 채널을 함께 이용할 수 있는 IRP', 0.0000, 0.0029, 'ETF 175종 + 펀드 1,550종 + 예금', 'https://www.kbsec.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('INSURANCE', '신한라이프', '신한라이프 연금저축보험', 'PENSION_SAVINGS_INSURANCE', 'INSURANCE_PRODUCT', '납입 기간과 연금 개시 시점을 폭넓게 고를 수 있는 보험형 연금', 0.0400, 0.0670, '공시이율 연동 (투자상품 선택 없음)', 'https://www.shinhanlife.co.kr', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('INSURANCE', '하나생명', '하나생명 연금저축보험', 'PENSION_SAVINGS_INSURANCE', 'INSURANCE_PRODUCT', '소액 정액 납입으로 부담 없이 시작하기 좋은 보험형 연금', 0.0400, 0.0690, '공시이율 연동 (투자상품 선택 없음)', 'https://www.hanalife.co.kr', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01');

INSERT INTO product_features (product_id, display_order, content)
SELECT id, 1, '원리금보장 예금 비중을 자유롭게 조절' FROM pension_products WHERE provider_name = '하나은행' AND product_name = '하나은행 개인형 IRP'
 UNION ALL SELECT id, 2, '영업점과 앱에서 모두 관리 가능' FROM pension_products WHERE provider_name = '하나은행' AND product_name = '하나은행 개인형 IRP'
 UNION ALL SELECT id, 3, '납입 자동이체와 연금 알림 제공' FROM pension_products WHERE provider_name = '하나은행' AND product_name = '하나은행 개인형 IRP'
 UNION ALL SELECT id, 1, 'ETF 와 예금을 한 계좌에서 혼합 운용' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 개인형 IRP'
 UNION ALL SELECT id, 2, '안전자산 30% 자동 편입 지원' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 개인형 IRP'
 UNION ALL SELECT id, 3, '계열 은행 계좌와 연계 이체' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 개인형 IRP'
 UNION ALL SELECT id, 1, '납입 기간을 여러 구간에서 선택 가능' FROM pension_products WHERE provider_name = '신한라이프' AND product_name = '신한라이프 연금저축보험'
 UNION ALL SELECT id, 2, '공시이율에 따라 적립금이 쌓임' FROM pension_products WHERE provider_name = '신한라이프' AND product_name = '신한라이프 연금저축보험'
 UNION ALL SELECT id, 3, '연금 개시 시점을 조정할 수 있음' FROM pension_products WHERE provider_name = '신한라이프' AND product_name = '신한라이프 연금저축보험'
 UNION ALL SELECT id, 1, '소액부터 정액 납입이 가능함' FROM pension_products WHERE provider_name = '하나생명' AND product_name = '하나생명 연금저축보험'
 UNION ALL SELECT id, 2, '공시이율에 따라 적립금이 쌓임' FROM pension_products WHERE provider_name = '하나생명' AND product_name = '하나생명 연금저축보험'
 UNION ALL SELECT id, 3, '연금 수령 방식 선택 가능' FROM pension_products WHERE provider_name = '하나생명' AND product_name = '하나생명 연금저축보험';

INSERT INTO product_cautions (product_id, display_order, content)
SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = '하나은행' AND product_name = '하나은행 개인형 IRP'
 UNION ALL SELECT id, 2, '투자상품 라인업은 증권사보다 좁음' FROM pension_products WHERE provider_name = '하나은행' AND product_name = '하나은행 개인형 IRP'
 UNION ALL SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 개인형 IRP'
 UNION ALL SELECT id, 2, '해지 시 세금상 불이익 발생 가능' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 개인형 IRP'
 UNION ALL SELECT id, 1, '초기 사업비가 차감됨' FROM pension_products WHERE provider_name = '신한라이프' AND product_name = '신한라이프 연금저축보험'
 UNION ALL SELECT id, 2, '중도 해지 시 환급금이 납입액보다 적을 수 있음' FROM pension_products WHERE provider_name = '신한라이프' AND product_name = '신한라이프 연금저축보험'
 UNION ALL SELECT id, 1, '초기 사업비가 차감됨' FROM pension_products WHERE provider_name = '하나생명' AND product_name = '하나생명 연금저축보험'
 UNION ALL SELECT id, 2, '중도 해지 시 환급금이 납입액보다 적을 수 있음' FROM pension_products WHERE provider_name = '하나생명' AND product_name = '하나생명 연금저축보험';
