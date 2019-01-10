ALTER TABLE `student_profile_config`
  DROP INDEX `UK_caf8vd8kho1w6ktxn9ay333tf`,
  DROP FOREIGN KEY `FK_nvqro3hq0lb117ml2qcb9i0kk`,
  DROP COLUMN `learning_evidence`,
  ADD COLUMN `competence_evidence` bigint(20) DEFAULT NULL,
  ADD COLUMN `credential_profile_config_target_credential` bigint(20) DEFAULT NULL;

ALTER TABLE `student_profile_config`
  ADD CONSTRAINT `FK_h3y62ltbsptnfmn2ufms8xtqf` FOREIGN KEY (`competence_evidence`) REFERENCES `competence_evidence` (`id`),
  ADD UNIQUE KEY `UK_r9e7picmrna9hofn02wea0sol` (competence_profile_config, competence_evidence),
  ADD UNIQUE KEY `UK_7wwlpww7a1tf0siok7qc4ld7c` (credential_profile_config_target_credential),
  ADD CONSTRAINT `FK_7wwlpww7a1tf0siok7qc4ld7c` FOREIGN KEY (`credential_profile_config_target_credential`) REFERENCES `target_credential1` (`id`);