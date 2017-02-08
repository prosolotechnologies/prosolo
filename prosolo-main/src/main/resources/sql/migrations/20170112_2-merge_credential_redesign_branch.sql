ALTER TABLE `activity1`
	DROP FOREIGN KEY `FK_qa91f38y8rx85h4hx8s9hi57l`;

ALTER TABLE `activity1` 
	DROP COLUMN `draft`,
	DROP COLUMN `has_draft`,
	DROP COLUMN `draft_version`;

DROP TABLE `activity_assessment`;

RENAME TABLE `activity_discussion` TO `activity_assessment`;

ALTER TABLE `activity_discussion_participant` CHANGE `is_read` `is_read` CHAR(1) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT 'T';

ALTER TABLE `competence1` DROP FOREIGN KEY `FK_f73a2nr7rwamd8fda1wkb8oib`;

ALTER TABLE `competence1` 
	DROP COLUMN `draft`,
	DROP COLUMN `has_draft`,
	DROP COLUMN `draft_version`,
	DROP COLUMN `visible`,
  DROP COLUMN `scheduled_public_date`,
	ADD COLUMN `visible_to_all` char(1) DEFAULT 'F';

--
-- Table structure for table `user_group`
--

CREATE TABLE `user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `default_group` char(1) DEFAULT 'F',
  `name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- Indexes for table `user_group`
--
ALTER TABLE `user_group`
  ADD PRIMARY KEY (`id`);

CREATE TABLE `competence_user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `privilege` varchar(255) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `user_group` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Indexes for table `competence_user_group`
--
ALTER TABLE `competence_user_group`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_nag1itygmr161n1rvu2yayt7x` (`competence`,`user_group`),
  ADD KEY `FK_4b5s1r5gcdytwdbp2pr4rn4v5` (`user_group`);

-- Constraints for table `competence_user_group`
--
ALTER TABLE `competence_user_group`
  ADD CONSTRAINT `FK_60mk2r1qk5llr3p901h5705y6` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);

ALTER TABLE `competence_user_group`
  ADD CONSTRAINT `FK_4b5s1r5gcdytwdbp2pr4rn4v5` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`);
--

ALTER TABLE `credential1` DROP FOREIGN KEY `FK_q8uejthq8hky52fw7ebk3scnx`;

ALTER TABLE `credential1` 
	DROP COLUMN `draft`,
	DROP COLUMN `has_draft`,
	DROP COLUMN `draft_version`,
	DROP COLUMN `visible`;

ALTER TABLE `credential1`	
	ADD COLUMN `visible_to_all` char(1) DEFAULT 'F';

ALTER TABLE `credential1` CHANGE `scheduled_public_date` `scheduled_publish_date` DATETIME NULL DEFAULT NULL;

--
-- Table structure for table `credential_user_group`
--

CREATE TABLE `credential_user_group` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `privilege` varchar(255) DEFAULT NULL,
  `credential` bigint(20) NOT NULL,
  `user_group` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Indexes for table `credential_user_group`
--
ALTER TABLE `credential_user_group`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_s361d3r0uusedvnblr1kg5s0s` (`credential`,`user_group`),
  ADD KEY `FK_m5cvxlf67t55is1xw2gxfar0i` (`user_group`);

-- Constraints for table `credential_user_group`
--
ALTER TABLE `credential_user_group`
  ADD CONSTRAINT `FK_30jl08j0war3rn3u23m10uyq8` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);

ALTER TABLE `credential_user_group`
  ADD CONSTRAINT `FK_m5cvxlf67t55is1xw2gxfar0i` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`);
--


ALTER TABLE `outcome` CHANGE `result` `result` INT NULL DEFAULT NULL;

-- --------------------------------------------------------

--
-- Table structure for table `user_group_user`
--

CREATE TABLE `user_group_user` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `user_group` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Indexes for table `user_group_user`
--
ALTER TABLE `user_group_user`
  ADD KEY `FK_iifontadv5293tvuvf7r9e9u8` (`user_group`),
  ADD KEY `FK_kqb30gm40pn64yo6sgh0jgpcf` (`user`);

-- Constraints for table `user_group_user`
--
ALTER TABLE `user_group_user`
  ADD CONSTRAINT `FK_kqb30gm40pn64yo6sgh0jgpcf` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_iifontadv5293tvuvf7r9e9u8` FOREIGN KEY (`user_group`) REFERENCES `user_group` (`id`);

ALTER TABLE `user_group_user`
  ADD PRIMARY KEY (`id`);
