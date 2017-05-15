ALTER TABLE `credential_user_group`
  DROP INDEX `UK_s361d3r0uusedvnblr1kg5s0s`,
  ADD UNIQUE KEY `UK_s361d3r0uusedvnblr1kg5s0s` (`credential`,`user_group`, `privilege`);