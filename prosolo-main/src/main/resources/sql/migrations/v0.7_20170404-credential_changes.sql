ALTER TABLE `credential1`
	ADD COLUMN `archived` char(1) COLLATE utf8_bin DEFAULT 'F',
	ADD COLUMN `version` bigint(20) DEFAULT 0;