ALTER TABLE `user`
	ADD COLUMN `organization` bigint(20) DEFAULT NULL;

ALTER TABLE `user`
  ADD KEY `FK_organization` (`organization`);

ALTER TABLE `user`
  ADD CONSTRAINT `FK_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

CREATE TABLE `organization` (
	`id` bigint(20) NOT NULL,
	`created` datetime DEFAULT NULL,
	`deleted` char(1) DEFAULT 'F',
  `description` longtext,
	`title` varchar(1000) DEFAULT NULL
);

ALTER TABLE `organization`
	ADD PRIMARY KEY (`id`);
	
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
	ADD KEY `FK_organization` (`organization`),
	ADD KEY `FK_parent_unit` (`parent_unit`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_parent_unit` FOREIGN KEY (`parent_unit`) REFERENCES `unit` (`id`);
 
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
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_unit_role_membership` (`role`,`unit`,`user`),
  ADD KEY `FK_user` (`user`),
  ADD KEY `FK_unit1` (`unit`),
  ADD KEY `FK_role` (`role`);
  
ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`);
  
ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit1` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);
  
ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_role` FOREIGN KEY (`role`) REFERENCES `role` (`id`);

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
  ADD UNIQUE KEY `UK_credential_unit` (`credential`,`unit`),
  ADD KEY `FK_credential` (`credential`),
  ADD KEY `FK_unit2` (`unit`);
 
ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);

ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_unit2` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);
  
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
  ADD UNIQUE KEY `UK_competene_unit` (`competence`,`unit`),
  ADD KEY `FK_competence` (`competence`),
  ADD KEY `FK_unit3` (`unit`);
  
ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);
  
ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_unit3` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);
