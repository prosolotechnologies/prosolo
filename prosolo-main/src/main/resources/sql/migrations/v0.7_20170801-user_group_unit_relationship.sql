ALTER TABLE `user_group`
  ADD COLUMN `unit` bigint(20) DEFAULT NULL,
  ADD CONSTRAINT `FK_user_group_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);