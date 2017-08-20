ALTER TABLE `credential_instructor`
  ADD UNIQUE KEY `UK_credential_instructor_user_credential` (user,credential);