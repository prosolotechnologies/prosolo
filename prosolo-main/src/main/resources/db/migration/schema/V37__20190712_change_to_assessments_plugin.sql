ALTER TABLE `organization_plugin`
    ADD COLUMN `assessment_tokens_enabled` bit(1) NOT NULL DEFAULT 0,
    ADD COLUMN `private_discussion_enabled` bit(1) NOT NULL DEFAULT 0;