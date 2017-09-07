ALTER TABLE `competence_assessment`
  ADD UNIQUE KEY `UK_competence_assessment_target_competence_credential_assessment` (`credential_assessment`, `target_competence`);

ALTER TABLE `activity_assessment`
  ADD UNIQUE KEY `UK_activity_assessment_target_activity_competence_assessment` (`competence_assessment`, `target_activity`);

