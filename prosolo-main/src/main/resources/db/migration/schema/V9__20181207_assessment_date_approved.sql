ALTER TABLE `credential_assessment`
  ADD COLUMN `date_approved` datetime DEFAULT NULL;

ALTER TABLE `competence_assessment`
  ADD COLUMN `date_approved` datetime DEFAULT NULL;