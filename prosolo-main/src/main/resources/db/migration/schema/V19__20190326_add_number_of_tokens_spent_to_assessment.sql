ALTER TABLE `credential_assessment`
  ADD COLUMN `number_of_tokens_spent` int(11) NOT NULL DEFAULT 0;

ALTER TABLE `competence_assessment`
  ADD COLUMN `number_of_tokens_spent` int(11) NOT NULL DEFAULT 0;