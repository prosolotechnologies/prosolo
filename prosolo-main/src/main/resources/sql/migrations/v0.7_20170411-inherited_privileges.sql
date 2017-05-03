ALTER TABLE `competence_user_group`
	ADD COLUMN `inherited_from` bigint(20),
	ADD CONSTRAINT `FK_inheritedFrom` FOREIGN KEY (`inherited_from`) REFERENCES `credential1` (`id`),
	DROP FOREIGN KEY `FK_60mk2r1qk5llr3p901h5705y6`,
	DROP INDEX `UK_nag1itygmr161n1rvu2yayt7x`;

ALTER TABLE `competence_user_group`
	ADD CONSTRAINT `FK_60mk2r1qk5llr3p901h5705y6` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);