INSERT INTO pension_products (provider_type, provider_name, product_name, account_type, product_type, summary, fee_min_rate, fee_max_rate, investment_scope, official_url, data_source, reference_date) VALUES
    ('SECURITIES', '미래에셋증권', '미래에셋 연금저축펀드', 'PENSION_SAVINGS_FUND', 'FUND_ACCOUNT', '국내 최대 규모의 연금전용 펀드 라인업. ETF·펀드 선택 폭 넓음', 0.0015, 0.0045, 'ETF 300종 + 펀드 2,000종', 'https://securities.miraeasset.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', '삼성증권', '삼성증권 연금저축계좌', 'PENSION_SAVINGS_FUND', 'FUND_ACCOUNT', '삼성자산운용 ETF 라인업 + 투자 가이드 제공', 0.0012, 0.0040, 'ETF 200종 + 펀드 1,800종', 'https://www.samsungpop.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', '한국투자증권', '한국투자증권 연금저축펀드', 'PENSION_SAVINGS_FUND', 'FUND_ACCOUNT', '업계 최저 수준 펀드 판매수수료, 해외ETF 강점', 0.0010, 0.0035, 'ETF 250종 + 펀드 1,500종', 'https://securities.koreainvestment.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', 'KB증권', 'KB증권 연금저축펀드', 'PENSION_SAVINGS_FUND', 'FUND_ACCOUNT', '은행 계열 연계와 연금 상담 채널이 넓은 계좌', 0.0014, 0.0042, 'ETF 180종 + 펀드 1,600종', 'https://www.kbsec.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', 'NH투자증권', 'NH투자증권 연금저축펀드', 'PENSION_SAVINGS_FUND', 'FUND_ACCOUNT', 'TDF 중심 자동 배분 상품 구성이 잘 갖춰진 계좌', 0.0013, 0.0041, 'ETF 170종 + 펀드 1,400종', 'https://www.nhqv.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', '미래에셋증권', '미래에셋 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', '퇴직금과 개인 납입을 한 계좌에서 관리', 0.0000, 0.0030, 'ETF 280종 + 펀드 1,900종 + 예금', 'https://securities.miraeasset.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('SECURITIES', '삼성증권', '삼성증권 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', 'IRP 운용관리 수수료 면제 구간이 있는 계좌', 0.0000, 0.0028, 'ETF 190종 + 펀드 1,700종 + 예금', 'https://www.samsungpop.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('BANK', 'KB국민은행', 'KB국민은행 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', '예금 중심으로 안전하게 운용하기 좋은 IRP', 0.0000, 0.0035, '예금 + 펀드 400종', 'https://www.kbstar.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('BANK', '신한은행', '신한은행 개인형 IRP', 'INDIVIDUAL_IRP', 'IRP_ACCOUNT', '자동이체와 연금 관리 알림이 편리한 은행 IRP', 0.0000, 0.0033, '예금 + 펀드 350종', 'https://bank.shinhan.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('INSURANCE', '삼성생명', '삼성생명 연금저축보험', 'PENSION_SAVINGS_INSURANCE', 'INSURANCE_PRODUCT', '정해진 방식에 따라 매달 꾸준히 쌓는 보험형 연금', 0.0400, 0.0700, '공시이율 연동 (투자상품 선택 없음)', 'https://www.samsunglife.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('INSURANCE', '한화생명', '한화생명 연금저축보험', 'PENSION_SAVINGS_INSURANCE', 'INSURANCE_PRODUCT', '납입 유예 옵션이 있어 소득 변동에 대응하기 쉬움', 0.0400, 0.0680, '공시이율 연동 (투자상품 선택 없음)', 'https://www.hanwhalife.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01'),
    ('INSURANCE', '교보생명', '교보생명 연금저축보험', 'PENSION_SAVINGS_INSURANCE', 'INSURANCE_PRODUCT', '최저보증이율이 있어 하락장에서도 적립금이 유지됨', 0.0400, 0.0650, '공시이율 연동 (투자상품 선택 없음)', 'https://www.kyobo.com', '임시 데이터 · 금융회사 공식 채널 확인 전', DATE '2026-08-01');

INSERT INTO product_features (product_id, display_order, content)
SELECT id, 1, 'ETF·펀드 2,300종 이상 선택 가능' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 연금저축펀드'
 UNION ALL SELECT id, 2, '모바일 앱에서 실시간 포트폴리오 확인' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 연금저축펀드'
 UNION ALL SELECT id, 3, '자동 리밸런싱 서비스 제공' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 연금저축펀드'
 UNION ALL SELECT id, 1, '초보자용 포트폴리오 가이드 제공' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 연금저축계좌'
 UNION ALL SELECT id, 2, '정기 리포트로 운용 현황 확인' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 연금저축계좌'
 UNION ALL SELECT id, 3, '앱에서 자동 이체 설정 가능' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 연금저축계좌'
 UNION ALL SELECT id, 1, '해외 ETF 라인업이 넓음' FROM pension_products WHERE provider_name = '한국투자증권' AND product_name = '한국투자증권 연금저축펀드'
 UNION ALL SELECT id, 2, '펀드 판매수수료가 낮은 편' FROM pension_products WHERE provider_name = '한국투자증권' AND product_name = '한국투자증권 연금저축펀드'
 UNION ALL SELECT id, 3, '연금 전용 리서치 자료 제공' FROM pension_products WHERE provider_name = '한국투자증권' AND product_name = '한국투자증권 연금저축펀드'
 UNION ALL SELECT id, 1, '영업점 연금 상담 이용 가능' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 연금저축펀드'
 UNION ALL SELECT id, 2, '계열 은행 계좌와 연계 이체' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 연금저축펀드'
 UNION ALL SELECT id, 3, '연금 전용 MTS 화면 제공' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 연금저축펀드'
 UNION ALL SELECT id, 1, '목표시점별 TDF 라인업 제공' FROM pension_products WHERE provider_name = 'NH투자증권' AND product_name = 'NH투자증권 연금저축펀드'
 UNION ALL SELECT id, 2, '자동 분산 투자 설정 가능' FROM pension_products WHERE provider_name = 'NH투자증권' AND product_name = 'NH투자증권 연금저축펀드'
 UNION ALL SELECT id, 3, '연금 자산 진단 도구 제공' FROM pension_products WHERE provider_name = 'NH투자증권' AND product_name = 'NH투자증권 연금저축펀드'
 UNION ALL SELECT id, 1, '퇴직금과 개인 납입을 함께 운용' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 개인형 IRP'
 UNION ALL SELECT id, 2, '위험자산 비중 70% 한도 자동 관리' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 개인형 IRP'
 UNION ALL SELECT id, 3, '예금·펀드·ETF 혼합 운용 가능' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 개인형 IRP'
 UNION ALL SELECT id, 1, '비대면 개설 시 수수료 우대 구간 존재' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 개인형 IRP'
 UNION ALL SELECT id, 2, '안전자산 30% 자동 편입 지원' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 개인형 IRP'
 UNION ALL SELECT id, 3, '퇴직금 이전 절차 지원' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 개인형 IRP'
 UNION ALL SELECT id, 1, '원리금보장 예금 라인업이 넓음' FROM pension_products WHERE provider_name = 'KB국민은행' AND product_name = 'KB국민은행 개인형 IRP'
 UNION ALL SELECT id, 2, '영업점과 앱 모두 이용 가능' FROM pension_products WHERE provider_name = 'KB국민은행' AND product_name = 'KB국민은행 개인형 IRP'
 UNION ALL SELECT id, 3, '급여이체 고객 수수료 우대 구간' FROM pension_products WHERE provider_name = 'KB국민은행' AND product_name = 'KB국민은행 개인형 IRP'
 UNION ALL SELECT id, 1, '납입 자동이체 설정이 간편함' FROM pension_products WHERE provider_name = '신한은행' AND product_name = '신한은행 개인형 IRP'
 UNION ALL SELECT id, 2, '연금 수령 시점 안내 알림 제공' FROM pension_products WHERE provider_name = '신한은행' AND product_name = '신한은행 개인형 IRP'
 UNION ALL SELECT id, 3, '원리금보장 상품 비중 조절 가능' FROM pension_products WHERE provider_name = '신한은행' AND product_name = '신한은행 개인형 IRP'
 UNION ALL SELECT id, 1, '매월 정액 납입으로 관리가 단순함' FROM pension_products WHERE provider_name = '삼성생명' AND product_name = '삼성생명 연금저축보험'
 UNION ALL SELECT id, 2, '공시이율에 따라 적립금이 쌓임' FROM pension_products WHERE provider_name = '삼성생명' AND product_name = '삼성생명 연금저축보험'
 UNION ALL SELECT id, 3, '종신 연금 수령 옵션 선택 가능' FROM pension_products WHERE provider_name = '삼성생명' AND product_name = '삼성생명 연금저축보험'
 UNION ALL SELECT id, 1, '일정 조건에서 납입 유예 신청 가능' FROM pension_products WHERE provider_name = '한화생명' AND product_name = '한화생명 연금저축보험'
 UNION ALL SELECT id, 2, '공시이율에 따라 적립금이 쌓임' FROM pension_products WHERE provider_name = '한화생명' AND product_name = '한화생명 연금저축보험'
 UNION ALL SELECT id, 3, '연금 개시 시점 조정 가능' FROM pension_products WHERE provider_name = '한화생명' AND product_name = '한화생명 연금저축보험'
 UNION ALL SELECT id, 1, '최저보증이율 적용 구간 존재' FROM pension_products WHERE provider_name = '교보생명' AND product_name = '교보생명 연금저축보험'
 UNION ALL SELECT id, 2, '시장 하락과 무관하게 적립금 유지' FROM pension_products WHERE provider_name = '교보생명' AND product_name = '교보생명 연금저축보험'
 UNION ALL SELECT id, 3, '연금 수령 방식 선택 가능' FROM pension_products WHERE provider_name = '교보생명' AND product_name = '교보생명 연금저축보험';

INSERT INTO product_cautions (product_id, display_order, content)
SELECT id, 1, '일부 펀드는 판매수수료 별도' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 연금저축펀드'
 UNION ALL SELECT id, 2, 'ETF 거래 시 매매 수수료 발생' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 연금저축펀드'
 UNION ALL SELECT id, 1, '일부 펀드는 판매수수료 별도' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 연금저축계좌'
 UNION ALL SELECT id, 2, '가이드 포트폴리오는 투자 권유가 아님' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 연금저축계좌'
 UNION ALL SELECT id, 1, '해외자산은 환율 변동 영향을 받음' FROM pension_products WHERE provider_name = '한국투자증권' AND product_name = '한국투자증권 연금저축펀드'
 UNION ALL SELECT id, 2, '일부 상품은 별도 신청 절차 필요' FROM pension_products WHERE provider_name = '한국투자증권' AND product_name = '한국투자증권 연금저축펀드'
 UNION ALL SELECT id, 1, '상담 채널 이용 시간이 제한됨' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 연금저축펀드'
 UNION ALL SELECT id, 2, '일부 펀드는 판매수수료 별도' FROM pension_products WHERE provider_name = 'KB증권' AND product_name = 'KB증권 연금저축펀드'
 UNION ALL SELECT id, 1, 'TDF는 시점별 위험 수준이 달라짐' FROM pension_products WHERE provider_name = 'NH투자증권' AND product_name = 'NH투자증권 연금저축펀드'
 UNION ALL SELECT id, 2, '일부 펀드는 판매수수료 별도' FROM pension_products WHERE provider_name = 'NH투자증권' AND product_name = 'NH투자증권 연금저축펀드'
 UNION ALL SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 개인형 IRP'
 UNION ALL SELECT id, 2, '해지 시 세금상 불이익 발생 가능' FROM pension_products WHERE provider_name = '미래에셋증권' AND product_name = '미래에셋 개인형 IRP'
 UNION ALL SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 개인형 IRP'
 UNION ALL SELECT id, 2, '해지 시 세금상 불이익 발생 가능' FROM pension_products WHERE provider_name = '삼성증권' AND product_name = '삼성증권 개인형 IRP'
 UNION ALL SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = 'KB국민은행' AND product_name = 'KB국민은행 개인형 IRP'
 UNION ALL SELECT id, 2, '투자상품 라인업은 증권사보다 좁음' FROM pension_products WHERE provider_name = 'KB국민은행' AND product_name = 'KB국민은행 개인형 IRP'
 UNION ALL SELECT id, 1, '중도인출은 법정 사유에만 가능' FROM pension_products WHERE provider_name = '신한은행' AND product_name = '신한은행 개인형 IRP'
 UNION ALL SELECT id, 2, '투자상품 라인업은 증권사보다 좁음' FROM pension_products WHERE provider_name = '신한은행' AND product_name = '신한은행 개인형 IRP'
 UNION ALL SELECT id, 1, '초기 사업비가 차감되어 원금 회복에 시간이 걸림' FROM pension_products WHERE provider_name = '삼성생명' AND product_name = '삼성생명 연금저축보험'
 UNION ALL SELECT id, 2, '중도 해지 시 환급금이 납입액보다 적을 수 있음' FROM pension_products WHERE provider_name = '삼성생명' AND product_name = '삼성생명 연금저축보험'
 UNION ALL SELECT id, 1, '초기 사업비가 차감됨' FROM pension_products WHERE provider_name = '한화생명' AND product_name = '한화생명 연금저축보험'
 UNION ALL SELECT id, 2, '중도 해지 시 환급금이 납입액보다 적을 수 있음' FROM pension_products WHERE provider_name = '한화생명' AND product_name = '한화생명 연금저축보험'
 UNION ALL SELECT id, 1, '초기 사업비가 차감됨' FROM pension_products WHERE provider_name = '교보생명' AND product_name = '교보생명 연금저축보험'
 UNION ALL SELECT id, 2, '중도 해지 시 환급금이 납입액보다 적을 수 있음' FROM pension_products WHERE provider_name = '교보생명' AND product_name = '교보생명 연금저축보험';
