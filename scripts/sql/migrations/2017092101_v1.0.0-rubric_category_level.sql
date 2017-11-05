CREATE TABLE `category_level` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `category` bigint(20) NOT NULL,
    `level` bigint(20) NOT NULL
);

ALTER TABLE `category_level`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_category_level_category_level` (`category`,`level`),
  ADD CONSTRAINT `FK_category_level_category` FOREIGN KEY (`category`) REFERENCES `category` (`id`),
  ADD CONSTRAINT `FK_category_level_level` FOREIGN KEY (`level`) REFERENCES `level` (`id`);