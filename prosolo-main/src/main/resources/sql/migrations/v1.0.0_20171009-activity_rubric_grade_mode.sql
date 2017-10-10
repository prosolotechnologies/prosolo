ALTER TABLE `activity1`
    ADD COLUMN `grading_mode` varchar(255) NOT NULL DEFAULT 'MANUAL',
    DROP COLUMN `autograde`;