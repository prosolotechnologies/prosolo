ALTER TABLE `unit`
  ADD COLUMN `welcome_message` text;

ALTER TABLE `social_activity1`
  ADD COLUMN `unit` bigint(20);

ALTER TABLE `social_activity1`
  ADD CONSTRAINT `FK_social_activity1_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);