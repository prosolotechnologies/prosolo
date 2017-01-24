ALTER TABLE `user_group`
	ADD COLUMN `join_url_active` char(1) COLLATE utf8_bin DEFAULT 'F',
	ADD COLUMN `join_url_password` varchar(255) COLLATE utf8_bin DEFAULT NULL;
	
ALTER TABLE `user_group_user`
  DROP INDEX `FK_kqb30gm40pn64yo6sgh0jgpcf`,
  ADD UNIQUE KEY `UK_c63cjs9t3jl6nrf3asi7iej52` (`user`,`user_group`);