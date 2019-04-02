ALTER TABLE social_activity1
  ADD COLUMN `credential` bigint(20) DEFAULT NULL;

ALTER TABLE `social_activity1`
  ADD CONSTRAINT `FK_social_activity1_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);