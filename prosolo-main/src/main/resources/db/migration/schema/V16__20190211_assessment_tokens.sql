ALTER TABLE `organization`
  ADD COLUMN `assessment_tokens_enabled` bit(1) NOT NULL DEFAULT 0,
  ADD COLUMN `initial_number_of_tokens_given` int(11) NOT NULL DEFAULT 0,
  ADD COLUMN `number_of_spent_tokens_per_request` int(11) NOT NULL DEFAULT 0,
  ADD COLUMN `number_of_earned_tokens_per_assessment` int(11) NOT NULL DEFAULT 0;

ALTER TABLE `user`
  ADD COLUMN `available_for_assessments` bit(1) NOT NULL DEFAULT 0,
  ADD COLUMN `number_of_tokens` int(11) NOT NULL DEFAULT 0;

ALTER TABLE `credential_assessment`
  ADD COLUMN `quit_date` datetime DEFAULT NULL;

ALTER TABLE `competence_assessment`
  ADD COLUMN `target_credential` bigint(20) DEFAULT NULL,
  ADD COLUMN `credential_assessment` bigint(20) DEFAULT NULL,
  ADD COLUMN `quit_date` datetime DEFAULT NULL;

ALTER TABLE `competence_assessment`
  ADD CONSTRAINT `FK_competence_assessment_target_credential` FOREIGN KEY (`target_credential`) REFERENCES `target_credential1` (`id`),
  ADD CONSTRAINT `FK_competence_assessment_credential_assessment` FOREIGN KEY (`credential_assessment`) REFERENCES `credential_assessment` (`id`),
  ADD UNIQUE KEY `UK_competence_assessment_credential_assessment_competence` (credential_assessment, competence);