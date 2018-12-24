ALTER TABLE `target_credential1`
    DROP COLUMN `hidden_from_profile`;

ALTER TABLE `target_competence1`
  DROP COLUMN `hidden_from_profile`;

CREATE TABLE `student_profile_config` (
  `dtype` tinyint NOT NULL,
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `grade` int(11),
  `max_grade` int(11),
  `student` bigint(20) NOT NULL,
  `target_credential` bigint(20) NOT NULL,
  `credential_profile_config` bigint(20) DEFAULT NULL,
  `competence_profile_config` bigint(20) DEFAULT NULL,
  `target_competence` bigint(20) DEFAULT NULL,
  `learning_evidence` bigint(20) DEFAULT NULL,
  `credential_assessment` bigint(20) DEFAULT NULL,
  `competence_assessment` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE `student_profile_config`
  ADD CONSTRAINT `FK_dtxw8gdbacfpcfilygm3kycum` FOREIGN KEY (`student`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_d0xafp1sm9wdx4i1tf6i91v8n` FOREIGN KEY (`target_credential`) REFERENCES `target_credential1` (`id`),
  ADD CONSTRAINT `FK_5kggq0hv668xgdvgop3iaikgh` FOREIGN KEY (`competence_assessment`) REFERENCES `competence_assessment` (`id`),
  ADD CONSTRAINT `FK_9cpx6lgbf4qfle56w5wgg4698` FOREIGN KEY (`competence_profile_config`) REFERENCES `student_profile_config` (`id`),
  ADD CONSTRAINT `FK_nvqro3hq0lb117ml2qcb9i0kk` FOREIGN KEY (`learning_evidence`) REFERENCES `learning_evidence` (`id`),
  ADD CONSTRAINT `FK_cr9tbbme1961c7vwtquop76vo` FOREIGN KEY (`credential_profile_config`) REFERENCES `student_profile_config` (`id`),
  ADD CONSTRAINT `FK_jm24oixhhbiv2lv6qc8ulxtql` FOREIGN KEY (`target_competence`) REFERENCES `target_competence1` (`id`),
  ADD CONSTRAINT `FK_5a988817t0dbsod3skxtu7tdb` FOREIGN KEY (`credential_assessment`) REFERENCES `credential_assessment` (`id`),
  ADD UNIQUE KEY `UK_d8b1436fuqqgy5frx5768ky00` (credential_profile_config, credential_assessment),
  ADD UNIQUE KEY `UK_k0rta0l6175hrweftjiv8rdq5` (credential_profile_config, target_competence),
  ADD UNIQUE KEY `UK_jabs2wf93d8nlwagpvx24j0kc` (competence_profile_config, competence_assessment),
  ADD UNIQUE KEY `UK_caf8vd8kho1w6ktxn9ay333tf` (competence_profile_config, learning_evidence);