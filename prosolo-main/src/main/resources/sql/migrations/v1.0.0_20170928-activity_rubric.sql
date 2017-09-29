ALTER TABLE `activity1`
    ADD COLUMN `rubric` bigint(20),
    ADD COLUMN `rubric_visibility` varchar(255) NOT NULL DEFAULT 'NEVER',
    ADD CONSTRAINT `FK_activity1_rubric` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`);