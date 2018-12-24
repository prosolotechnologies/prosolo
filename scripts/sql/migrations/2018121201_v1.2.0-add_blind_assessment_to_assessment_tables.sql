ALTER TABLE `credential_assessment`
  ADD COLUMN `blind_assessment_mode` varchar(255) NOT NULL DEFAULT 'OFF';

ALTER TABLE `competence_assessment`
  ADD COLUMN `blind_assessment_mode` varchar(255) NOT NULL DEFAULT 'OFF';

ALTER TABLE `competence_assessment_config`
  ADD COLUMN `blind_assessment_mode` varchar(255) NOT NULL DEFAULT 'OFF';