ALTER TABLE `organization`
  DROP COLUMN `learning_in_stages_enabled`,
  DROP COLUMN `assessment_tokens_enabled`,
  DROP COLUMN `initial_number_of_tokens_given`,
  DROP COLUMN `number_of_spent_tokens_per_request`,
  DROP COLUMN `number_of_earned_tokens_per_assessment`;

# this is executed after the data is migrated in the V__30 script
ALTER TABLE `learning_stage`
  ADD CONSTRAINT `FK_o9r7by6jdhxq2dec0r4kn5fd0` FOREIGN KEY (`learning_stages_plugin`) REFERENCES `organization_plugin` (`id`);

# this is executed after the data is migrated in the V__30 script
ALTER TABLE `credential_category`
  ADD CONSTRAINT `FK_q8w7t2dnw9ednh7vg20b4r5am` FOREIGN KEY (`credential_categories_plugin`) REFERENCES `organization_plugin` (`id`);