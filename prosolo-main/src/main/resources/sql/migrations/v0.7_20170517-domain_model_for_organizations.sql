CREATE TABLE `organization` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL
);

ALTER TABLE `organization`
    ADD PRIMARY KEY (`id`);

ALTER TABLE `user`
    ADD COLUMN `organization` bigint(20) DEFAULT NULL;

ALTER TABLE `user`
  ADD KEY `FK_user_organization` (`organization`);

ALTER TABLE `user`
  ADD CONSTRAINT `FK_user_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

CREATE TABLE `unit` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `organization` bigint(20) NOT NULL,
    `parent_unit` bigint(20) DEFAULT NULL
);

ALTER TABLE `unit`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK_unit_organization` (`organization`),
    ADD KEY `FK_unit_parent_unit` (`parent_unit`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_unit_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_unit_parent_unit` FOREIGN KEY (`parent_unit`) REFERENCES `unit` (`id`);

CREATE TABLE `unit_role_membership` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `role` bigint(20) NOT NULL,
    `unit` bigint(20) NOT NULL,
    `user` bigint(20) NOT NULL
);

ALTER TABLE `unit_role_membership`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_role` FOREIGN KEY (`role`) REFERENCES `role` (`id`);

ALTER TABLE `unit_role_membership`
  ADD UNIQUE KEY `UK_unit_role_membership` (`role`,`unit`,`user`);

CREATE TABLE `credential_unit` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `credential` bigint(20) NOT NULL,
    `unit` bigint(20) NOT NULL
);

ALTER TABLE `credential_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_credential_unit_credential_unit` (`credential`,`unit`),
  ADD KEY `FK_credential_unit_credential` (`credential`),
  ADD KEY `FK_credential_unit_unit` (`unit`);

ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_credential_unit_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);

ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_credential_unit_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

CREATE TABLE `competence_unit` (
    `id` bigint(20) NOT NULL,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(1000) DEFAULT NULL,
    `competence` bigint(20) NOT NULL,
    `unit` bigint(20) NOT NULL
);

ALTER TABLE `competence_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_competence_unit_competence_unit` (`competence`,`unit`),
  ADD KEY `FK_competence_unit_competence` (`competence`),
  ADD KEY `FK_competence_unit_unit` (`unit`);

ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_competence_unit_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);

ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_competence_unit_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);