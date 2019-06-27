CREATE TABLE `organization_plugin` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `enabled` char(1) DEFAULT 'T',
  `type` varchar(255) NOT NULL,
  `initial_number_of_tokens_given` int(11) DEFAULT NULL,
  `number_of_earned_tokens_per_assessment` int(11) DEFAULT NULL,
  `number_of_spent_tokens_per_request` int(11) DEFAULT NULL,
  `keywords_enabled` char(1) DEFAULT NULL,
  `file_evidence_enabled` char(1) DEFAULT NULL,
  `url_evidence_enabled` char(1) DEFAULT NULL,
  `text_evidence_enabled` char(1) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_n0inw6m1d2d7lqufhrf46i3p7` (`organization`),
  CONSTRAINT `FK_n0inw6m1d2d7lqufhrf46i3p7` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`)
);


ALTER TABLE `learning_stage`
  ADD COLUMN `learning_stages_plugin` bigint(20) NOT NULL;

ALTER TABLE `credential_category`
  ADD COLUMN `credential_categories_plugin` bigint(20) NOT NULL;

