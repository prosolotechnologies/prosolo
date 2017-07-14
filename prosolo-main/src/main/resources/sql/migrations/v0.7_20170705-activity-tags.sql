CREATE TABLE `activity1_tags` (
  `activity1` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL
);

ALTER TABLE `activity1_tags`
	ADD PRIMARY KEY (`activity1`,`tags`),
	ADD CONSTRAINT `FK_activity1_tags_tags` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
	ADD CONSTRAINT `FK_activity1_tags_activity1` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`);