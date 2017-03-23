ALTER TABLE `competence1`
  DROP COLUMN `can_be_edited`,
  ADD COLUMN `version` bigint(20) DEFAULT 0;
  
ALTER TABLE `activity1`
  DROP COLUMN `published`,
  ADD COLUMN `version` bigint(20) DEFAULT 0;