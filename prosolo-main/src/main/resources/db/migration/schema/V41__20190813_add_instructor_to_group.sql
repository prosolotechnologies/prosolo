CREATE TABLE `user_group_instructor` (
   `id` bigint(20) NOT NULL,
   `created` datetime DEFAULT NULL,
   `deleted` char(1) DEFAULT 'F',
   `description` longtext,
   `title` varchar(255) DEFAULT NULL,
   `user_group` bigint(20) NOT NULL,
   `user` bigint(20) NOT NULL,
   PRIMARY KEY (`id`)
);

ALTER TABLE `user_group_instructor`
    ADD CONSTRAINT `FK_user_group_instructor_user_group` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`),
    ADD CONSTRAINT `FK_user_group_instructor_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
    ADD UNIQUE KEY `UK_user_group_instructor_user_user_group` (`user`,`user_group`);

ALTER TABLE `credential_instructor`
    ADD COLUMN `status` varchar(255) NOT NULL DEFAULT 'ACTIVE';