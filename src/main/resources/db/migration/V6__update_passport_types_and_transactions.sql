UPDATE pension_passports SET type_code = 'FLEXIBLE_MAINTAINER' WHERE type_code = 'FLEXIBLE_BALANCER';
UPDATE pension_passports SET type_code = 'CASHFLOW_GUARDIAN' WHERE type_code = 'CAUTIOUS_GUARDIAN';
UPDATE pension_passports SET type_code = 'MARKET_SENSITIVE' WHERE type_code = 'REALISTIC_PLANNER';

ALTER TABLE pension_passports DROP CONSTRAINT ck_passports_type_code;
ALTER TABLE pension_passports ADD CONSTRAINT ck_passports_type_code CHECK (type_code IN
    ('STEADY_PIONEER', 'FLEXIBLE_MAINTAINER', 'CASHFLOW_GUARDIAN', 'MARKET_SENSITIVE', 'LONG_TERM_KEEPER'));

ALTER TABLE pension_passports ADD COLUMN detailed_analysis_report TEXT;

ALTER TABLE spending_analyses ALTER COLUMN recurring_expense DROP NOT NULL;

DELETE FROM virtual_transactions;

INSERT INTO virtual_transactions (category_code, merchant_name, amount, transacted_at) VALUES
    ('FOOD_DELIVERY',  '스타벅스',     6500,  TIMESTAMPTZ '2026-08-24 08:10:00+09'),
    ('FOOD_DELIVERY',  '김밥천국',     9000,  TIMESTAMPTZ '2026-08-24 12:30:00+09'),
    ('FOOD_DELIVERY',  '배달의민족',   27000, TIMESTAMPTZ '2026-08-24 19:20:00+09'),
    ('FOOD_DELIVERY',  '메가커피',     4500,  TIMESTAMPTZ '2026-08-25 14:15:00+09'),
    ('OTHER',          'CGV',          15000, TIMESTAMPTZ '2026-08-25 21:10:00+09'),
    ('TRANSPORTATION', '카카오T',      14200, TIMESTAMPTZ '2026-08-26 08:45:00+09'),
    ('FOOD_DELIVERY',  '한솥도시락',   8500,  TIMESTAMPTZ '2026-08-26 13:05:00+09'),
    ('SUBSCRIPTION',   '넷플릭스',     13500, TIMESTAMPTZ '2026-08-26 16:40:00+09'),
    ('FOOD_DELIVERY',  '배달의민족',   24500, TIMESTAMPTZ '2026-08-27 12:20:00+09'),
    ('SHOPPING',       '올리브영',     32000, TIMESTAMPTZ '2026-08-27 18:30:00+09'),
    ('TRANSPORTATION', '지하철 정기권', 8800, TIMESTAMPTZ '2026-08-28 09:25:00+09'),
    ('FOOD_DELIVERY',  '배달의민족',   31000, TIMESTAMPTZ '2026-08-28 19:50:00+09'),
    ('SUBSCRIPTION',   '유튜브 프리미엄', 5500, TIMESTAMPTZ '2026-08-29 15:10:00+09'),
    ('SHOPPING',       '무신사',       23000, TIMESTAMPTZ '2026-08-29 20:40:00+09'),
    ('FOOD_DELIVERY',  '배달의민족',   29000, TIMESTAMPTZ '2026-08-30 11:35:00+09'),
    ('TRANSPORTATION', '카카오T',      5000,  TIMESTAMPTZ '2026-08-30 14:20:00+09');
