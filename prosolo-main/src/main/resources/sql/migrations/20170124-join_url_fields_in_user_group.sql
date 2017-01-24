ALTER TABLE `user_group`
	ADD COLUMN `join_url_active` char(1) COLLATE utf8_bin DEFAULT 'F',
	ADD COLUMN `join_url_password` varchar(255) COLLATE utf8_bin DEFAULT NULL;