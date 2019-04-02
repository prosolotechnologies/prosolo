ALTER TABLE social_activity1
  ADD COLUMN `parent_credential` bigint(20) DEFAULT NULL;

ALTER TABLE `social_activity1`
  ADD CONSTRAINT `FK_social_activity1_parent_credential` FOREIGN KEY (`parent_credential`) REFERENCES `credential1` (`id`);