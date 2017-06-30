ALTER TABLE `competence_assessment`
  ADD UNIQUE KEY `UK_target_competence_credential_assessment` (`credential_assessment`, `target_competence`);

ALTER TABLE `activity_assessment`
  ADD UNIQUE KEY `UK_target_activity_competence_assessment` (`competence_assessment`, `target_activity`);

