RENAME TABLE `category` TO `criterion`, `category_level` TO `criterion_level`;

ALTER TABLE `criterion`
  CHANGE COLUMN `category_order` `criterion_order` int(11) NOT NULL DEFAULT 1;

ALTER TABLE `criterion_level`
  CHANGE COLUMN `category` `criterion` bigint(20) NOT NULL,
  DROP INDEX `UK_category_level_category_level`,
  ADD UNIQUE KEY `UK_criterion_level_criterion_level` (`criterion`,`level`),
  DROP FOREIGN KEY `FK_category_level_category`,
  ADD CONSTRAINT `FK_criterion_level_criterion` FOREIGN KEY (`criterion`) REFERENCES `criterion` (`id`);
