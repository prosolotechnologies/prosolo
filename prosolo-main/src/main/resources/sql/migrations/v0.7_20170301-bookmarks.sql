CREATE TABLE `competence_bookmark` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL
);

ALTER TABLE `competence_bookmark`
    ADD PRIMARY KEY (`id`);

ALTER TABLE `competence_bookmark`
	ADD CONSTRAINT `FK_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
	ADD CONSTRAINT `FK_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
	ADD UNIQUE KEY `UK_competence_user` (`competence`,`user`);
	
ALTER TABLE `credential_bookmark`
	ADD UNIQUE KEY `UK_credential_user` (`credential`,`user`);

