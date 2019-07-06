ALTER TABLE `credential_assessment`
  MODIFY `student` bigint(20) NOT NULL,
  ADD COLUMN `status` varchar(255) NOT NULL;

ALTER TABLE `competence_assessment`
  ADD COLUMN `status` varchar(255) NOT NULL;