ALTER TABLE behavior_missions DROP CONSTRAINT ck_missions_target_amount;
ALTER TABLE behavior_missions ADD CONSTRAINT ck_missions_target_amount CHECK (target_amount > 0);
