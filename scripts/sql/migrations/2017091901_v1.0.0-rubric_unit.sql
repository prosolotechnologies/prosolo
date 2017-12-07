CREATE TABLE `rubric_unit` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `rubric` bigint(20) NOT NULL,
    `unit` bigint(20) NOT NULL
);

ALTER TABLE `rubric_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_rubric_unit_rubric_unit` (`rubric`,`unit`),
  ADD CONSTRAINT `FK_rubric_unit_rubric` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`),
  ADD CONSTRAINT `FK_rubric_unit_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);