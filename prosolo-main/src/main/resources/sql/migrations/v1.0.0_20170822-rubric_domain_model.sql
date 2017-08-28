CREATE TABLE `rubric` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(255) DEFAULT NULL,
    `creator` bigint(20) NOT NULL,
    `organization` bigint(20) NOT NULL
);

ALTER TABLE `rubric`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK_rubric_organization` (`organization`);

ALTER TABLE `rubric`
  ADD CONSTRAINT `FK_rubric_creator` FOREIGN KEY (`creator`) REFERENCES `user` (`id`);

ALTER TABLE `rubric`
  ADD CONSTRAINT `FK_rubric_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `rubric`
  ADD UNIQUE KEY `UK_rubric_title_organization` (title, organization);

CREATE TABLE `category` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(255) DEFAULT NULL,
    `points` double NOT NULL,
    `rubric` bigint(20) NOT NULL
);

ALTER TABLE `category`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK_category_rubric` (`rubric`);

ALTER TABLE `category`
  ADD UNIQUE KEY `UK_category_title_rubric` (title, rubric);

CREATE TABLE `level` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(255) DEFAULT NULL,
    `points` double NOT NULL,
    `rubric` bigint(20) NOT NULL
);

ALTER TABLE `level`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK_level_rubric` (`rubric`);

ALTER TABLE `level`
  ADD UNIQUE KEY `UK_level_title_rubric` (title, rubric);