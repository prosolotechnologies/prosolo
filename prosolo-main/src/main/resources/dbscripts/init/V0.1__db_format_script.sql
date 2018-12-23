ALTER TABLE `rubric`
  ADD UNIQUE KEY `UK_4fkcsmege20c5mr184i7sw8a8` (title(191), organization);

ALTER TABLE `criterion`
  ADD UNIQUE KEY `UK_e1f4a4t2ns27fm40ulwry89u8` (title(191), rubric);

ALTER TABLE `level`
  ADD UNIQUE KEY `UK_eo5k8jw0yq5kypsaddagxuoo8` (title(191), rubric);

ALTER TABLE `credential_assessment_config`
  ADD UNIQUE KEY `UK_e7upesftge7cjrbgqwqlbhypi` (credential, assessment_type(191));

ALTER TABLE `unit`
  ADD UNIQUE KEY `UK_hotnqwr5osir4ryygavjto7ac` (title(191), organization);

ALTER TABLE `organization`
  ADD UNIQUE KEY `UK_98elant9gwyioac8t0br67o5p` (title(191));

ALTER TABLE `learning_stage`
  ADD UNIQUE KEY `UK_atmtmfxbwira99uums40pt8q` (organization, title(191));

ALTER TABLE `notification_settings`
  ADD UNIQUE KEY `UK_fhuhqotesvv46234bv4r36w19` (type(191), user);

ALTER TABLE `credential_category`
  ADD UNIQUE KEY `UK_6mnq89yqpnfilldvrsyblvavb` (organization, title(191));

ALTER TABLE `credential_user_group`
  ADD UNIQUE KEY `UK_j07j1pph3s8y22el92gth2yy5` (credential, user_group, privilege(191));

ALTER TABLE `competence_assessment_config`
  ADD UNIQUE KEY `UK_7u112t9m0qt8cdf1pwd2t6nam` (competence, assessment_type(191));

ALTER TABLE `comment1`
  ADD INDEX `index_comment1_commented_resource_id_resource_type` (commented_resource_id,resource_type(191));

ALTER TABLE `registration_key`
  ADD UNIQUE KEY `UK_ran40qjbh70o6m8q25xu4qxc9` (uid(191));

ALTER TABLE `reset_key`
  ADD UNIQUE KEY `UK_4os64y5nsc6cbbd73qj4lujb1` (uid(191));