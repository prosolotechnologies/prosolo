CREATE TABLE `instructor_withdrawal` (
      `id` bigint(20) NOT NULL,
      `created` datetime DEFAULT NULL,
      `deleted` char(1) DEFAULT 'F',
      `description` longtext,
      `title` varchar(255) DEFAULT NULL,
      `target_credential` bigint(20) NOT NULL,
      `instructor` bigint(20) NOT NULL,
      PRIMARY KEY (`id`)
);

ALTER TABLE `instructor_withdrawal`
  ADD CONSTRAINT `FK_instructor_withdrawal_target_credential` FOREIGN KEY (`target_credential`) REFERENCES `target_credential1` (`id`),
  ADD CONSTRAINT `FK_instructor_withdrawal_instructor` FOREIGN KEY (`instructor`) REFERENCES `user` (`id`),
  ADD UNIQUE KEY `UK_instructor_withdrawal_target_credential_instructor` (target_credential, instructor);