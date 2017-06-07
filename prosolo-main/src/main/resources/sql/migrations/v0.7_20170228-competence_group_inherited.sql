ALTER TABLE `competence_user_group`
  ADD COLUMN `inherited` char(1) COLLATE utf8_bin DEFAULT 'F';
  
UPDATE `credential_user_group` 
	SET `privilege` = 'Learn' 
	WHERE `privilege` = 'View';
	
UPDATE `competence_user_group` 
	SET `privilege` = 'Learn' 
	WHERE `privilege` = 'View';