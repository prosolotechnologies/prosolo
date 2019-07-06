CREATE TABLE `profile_settings` (
  `id` bigint(20) NOT NULL,
  `custom_profile_url` varchar(60) NULL,
  `summary_sidebar_enabled` char(1) DEFAULT 'T',
  `user` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE `profile_settings`
  ADD CONSTRAINT `FK_profile_settings_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  ADD UNIQUE KEY `UK_profile_settings_user` (user),
  ADD UNIQUE KEY `UK_profile_settings_custom_profile_url` (custom_profile_url(60));