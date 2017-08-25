#20170125_1-activity-autograde.sql
ALTER TABLE `activity1`
  ADD COLUMN `autograde` char(1) COLLATE utf8_bin DEFAULT 'F';

#v0.7_20170127-activity-difficulty.sql
ALTER TABLE `activity1`
  ADD COLUMN `difficulty` int(11) DEFAULT 3;

#v0.7_20170207-manager_comment.sql
ALTER TABLE `comment1`
  ADD COLUMN `manager_comment` char(1) COLLATE utf8_bin DEFAULT 'F';

#v0.7_20170209-notification_object_owner.sql
ALTER TABLE `notification1`
  ADD COLUMN `object_owner` char(1) COLLATE utf8_bin DEFAULT 'F';

#v0.7_20170214-feed_digest_changes.sql
ALTER TABLE `feeds_digest`
  DROP FOREIGN KEY `FK_ctbkbo2ijgwfhhmh006v305yx`;

ALTER TABLE `feeds_digest`
  ADD COLUMN `date_from` datetime DEFAULT NULL,
  ADD COLUMN `date_to` datetime DEFAULT NULL,
  DROP COLUMN `course`,
  ADD COLUMN `credential` bigint(20) DEFAULT NULL,
  ADD CONSTRAINT `FK_ctbkbo2ijgwfhhmh006v305yx` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  ADD COLUMN `number_of_users_that_got_email` bigint(20) DEFAULT 0;

DROP TABLE `feeds_digest_tweets`;

#v0.7_20170227-credential_redesign.sql
ALTER TABLE `credential1`
  DROP FOREIGN KEY `FK_hkwb5xu2110cpd8k6t0cnb4si`;

ALTER TABLE `credential1`
  DROP COLUMN `published`,
  DROP COLUMN `scheduled_publish_date`,
  DROP COLUMN `students_can_add_competences`,
  DROP COLUMN `type`,
  DROP COLUMN `based_on`,
  ADD COLUMN `delivery_of` bigint(20),
  ADD COLUMN `delivery_start` datetime DEFAULT NULL,
  ADD COLUMN `delivery_end` datetime DEFAULT NULL,
  ADD COLUMN `type` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT 'Original',
  ADD CONSTRAINT `FK_credential1_delivery_of` FOREIGN KEY (`delivery_of`) REFERENCES `credential1` (`id`);

ALTER TABLE `competence1`
  ADD COLUMN `original_version` bigint(20),
  ADD COLUMN `date_published` datetime DEFAULT NULL,
  ADD COLUMN `can_be_edited` char(1) DEFAULT 'F',
  ADD CONSTRAINT `FK_competence1_originalVersion` FOREIGN KEY (`original_version`) REFERENCES `competence1` (`id`);

ALTER TABLE `activity1`
  DROP FOREIGN KEY `FK_sajnjcx3qxj6c34jgix4do7m4`;

DROP TABLE `grading_options`;

ALTER TABLE `activity1`
  DROP COLUMN `upload_assignment`,
  DROP COLUMN `grading_options`;

DROP TABLE `target_credential1_tags`;
DROP TABLE `target_credential1_hashtags`;

ALTER TABLE `target_credential1`
  DROP FOREIGN KEY `FK_ahas0wfv79n566ow6rnkj58ww`;

ALTER TABLE `target_credential1`
  DROP COLUMN `credential_type`,
  DROP COLUMN `duration`,
  DROP COLUMN `students_can_add_competences`,
  DROP COLUMN `competence_order_mandatory`,
  DROP COLUMN `created_by`,
  DROP COLUMN `next_activity_to_learn_id`;

DROP TABLE `target_competence1_tags`;

ALTER TABLE `target_competence1`
  DROP FOREIGN KEY `FK_7rju37g89o107d4c4xkvocf57`,
  DROP FOREIGN KEY `FK_jmleoxcqddn48ct9bw2uymj0m`;

ALTER TABLE `target_competence1`
  DROP COLUMN `type`,
  DROP COLUMN `duration`,
  DROP COLUMN `student_allowed_to_add_activities`,
  DROP COLUMN `comp_order`,
  DROP COLUMN `target_credential`,
  DROP COLUMN `added`,
  DROP COLUMN `created_by`,
  ADD COLUMN `user` bigint(20),
  ADD UNIQUE KEY `UK_target_competence1_competence_user` (`competence`,`user`);

DROP TABLE `target_activity1_links`;
DROP TABLE `target_activity1_files`;
DROP TABLE `target_activity1_captions`;

ALTER TABLE `target_activity1`
  DROP FOREIGN KEY `FK_7sb8h5hca0bkw3smwl4e2pq3r`;

ALTER TABLE `target_activity1`
  DROP COLUMN `dtype`,
  DROP COLUMN `duration`,
  DROP COLUMN `result_type`,
  DROP COLUMN `upload_assignment`,
  DROP COLUMN `assignment_link`,
  DROP COLUMN `assignment_title`,
  DROP COLUMN `created_by`,
  DROP COLUMN `learning_resource_type`,
  DROP COLUMN `launch_url`,
  DROP COLUMN `shared_secret`,
  DROP COLUMN `consumer_key`,
  DROP COLUMN `open_in_new_window`,
  DROP COLUMN `score_calculation`,
  DROP COLUMN `text`,
  DROP COLUMN `url`,
  DROP COLUMN `link_name`,
  DROP COLUMN `type`;

#v0.7_20170228-competence_group_inherited.sql
ALTER TABLE `competence_user_group`
  ADD COLUMN `inherited` char(1) COLLATE utf8_bin DEFAULT 'F';

UPDATE `credential_user_group`
SET `privilege` = 'Learn'
WHERE `privilege` = 'View';

UPDATE `competence_user_group`
SET `privilege` = 'Learn'
WHERE `privilege` = 'View';

#v0.7_20170301-bookmarks.sql
CREATE TABLE `competence_bookmark` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL
);

ALTER TABLE `competence_bookmark`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `competence_bookmark`
  ADD CONSTRAINT `FK_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  ADD CONSTRAINT `FK_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`),
  ADD UNIQUE KEY `UK_competence_bookmark_competence_user` (`competence`,`user`);

ALTER TABLE `credential_bookmark`
  ADD UNIQUE KEY `UK_credential_bookmark_credential_user` (`credential`,`user`);

#v0.7_20170302-archived-competence.sql
ALTER TABLE `competence1`
  ADD COLUMN `archived` char(1) COLLATE utf8_bin DEFAULT 'F';

#v0.7_20170315-comp-and-activity-changes.sql
ALTER TABLE `competence1`
  DROP COLUMN `can_be_edited`,
  ADD COLUMN `version` bigint(20) DEFAULT 0;

ALTER TABLE `activity1`
  DROP COLUMN `published`,
  ADD COLUMN `version` bigint(20) DEFAULT 0;

#v0.7_20170404-credential_changes.sql
ALTER TABLE `credential1`
  ADD COLUMN `archived` char(1) COLLATE utf8_bin DEFAULT 'F',
  ADD COLUMN `version` bigint(20) DEFAULT 0;

#v0.7_20170411-inherited_privileges.sql
ALTER TABLE `competence_user_group`
  ADD COLUMN `inherited_from` bigint(20),
  ADD CONSTRAINT `FK_inheritedFrom` FOREIGN KEY (`inherited_from`) REFERENCES `credential1` (`id`),
  DROP FOREIGN KEY `FK_60mk2r1qk5llr3p901h5705y6`,
  DROP INDEX `UK_nag1itygmr161n1rvu2yayt7x`;

ALTER TABLE `competence_user_group`
  ADD CONSTRAINT `FK_60mk2r1qk5llr3p901h5705y6` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);

#v0.7_20170515-credential-user-group-unique-constraint.sql
ALTER TABLE `credential_user_group`
  DROP INDEX `UK_s361d3r0uusedvnblr1kg5s0s`,
  ADD UNIQUE KEY `UK_s361d3r0uusedvnblr1kg5s0s` (`credential`,`user_group`, `privilege`);

#v0.7_20170517-domain_model_for_organizations.sql
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL
);

ALTER TABLE `organization`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `user`
  ADD COLUMN `organization` bigint(20) DEFAULT NULL;

ALTER TABLE `user`
  ADD KEY `FK_user_organization` (`organization`);

ALTER TABLE `user`
  ADD CONSTRAINT `FK_user_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

CREATE TABLE `unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `organization` bigint(20) NOT NULL,
  `parent_unit` bigint(20) DEFAULT NULL
);

ALTER TABLE `unit`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_unit_organization` (`organization`),
  ADD KEY `FK_unit_parent_unit` (`parent_unit`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_unit_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `unit`
  ADD CONSTRAINT `FK_parent_unit` FOREIGN KEY (`parent_unit`) REFERENCES `unit` (`id`);

CREATE TABLE `unit_role_membership` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `role` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL,
  `user` bigint(20) NOT NULL
);

ALTER TABLE `unit_role_membership`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_user` FOREIGN KEY (`user`) REFERENCES `user` (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

ALTER TABLE `unit_role_membership`
  ADD CONSTRAINT `FK_unit_role_membership_role` FOREIGN KEY (`role`) REFERENCES `role` (`id`);

ALTER TABLE `unit_role_membership`
  ADD UNIQUE KEY `UK_unit_role_membership` (`role`,`unit`,`user`);

CREATE TABLE `credential_unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `credential` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL
);

ALTER TABLE `credential_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_credential_unit_credential_unit` (`credential`,`unit`),
  ADD KEY `FK_credential_unit_credential` (`credential`),
  ADD KEY `FK_credential_unit_unit` (`unit`);

ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_credential_unit_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);

ALTER TABLE `credential_unit`
  ADD CONSTRAINT `FK_credential_unit_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

CREATE TABLE `competence_unit` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT 'F',
  `description` longtext,
  `title` varchar(1000) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `unit` bigint(20) NOT NULL
);

ALTER TABLE `competence_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_competence_unit_competence_unit` (`competence`,`unit`),
  ADD KEY `FK_competence_unit_competence` (`competence`),
  ADD KEY `FK_competence_unit_unit` (`unit`);

ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_competence_unit_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`);

ALTER TABLE `competence_unit`
  ADD CONSTRAINT `FK_competence_unit_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

#v0.7_20170608-assessment_constraints.sql
ALTER TABLE `competence_assessment`
  ADD UNIQUE KEY `UK_competence_assessment_target_competence_credential_assessment` (`credential_assessment`, `target_competence`);

ALTER TABLE `activity_assessment`
  ADD UNIQUE KEY `UK_activity_assessment_target_activity_competence_assessment` (`competence_assessment`, `target_activity`);

#v0.7_20170705-activity-tags.sql
CREATE TABLE `activity1_tags` (
  `activity1` bigint(20) NOT NULL,
  `tags` bigint(20) NOT NULL
);

ALTER TABLE `activity1_tags`
  ADD PRIMARY KEY (`activity1`,`tags`),
  ADD CONSTRAINT `FK_activity1_tags_tags` FOREIGN KEY (`tags`) REFERENCES `tag` (`id`),
  ADD CONSTRAINT `FK_activity1_tags_activity1` FOREIGN KEY (`activity1`) REFERENCES `activity1` (`id`);

#v0.7_20170705-unit_name_and_organization_name_contraint.sql
ALTER TABLE `unit`
  ADD UNIQUE KEY `UK_unit_title_organization` (title(100), organization);

ALTER TABLE `organization`
  ADD UNIQUE KEY `UK_organization_title` (title(100));

#v0.7_20170801-user_group_unit_relationship.sql
ALTER TABLE `user_group`
  ADD COLUMN `unit` bigint(20) DEFAULT NULL,
  ADD CONSTRAINT `FK_user_group_unit` FOREIGN KEY (`unit`) REFERENCES `unit` (`id`);

#v0.7_20170815-credential_and_competence_organization_relationship.sql
ALTER TABLE `credential1`
  ADD COLUMN `organization` bigint(20) DEFAULT NULL,
  ADD CONSTRAINT `FK_credential1_credential_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `competence1`
  ADD COLUMN `organization` bigint(20) DEFAULT NULL,
  ADD CONSTRAINT `FK_competence_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

#v0.7_20171107-credential_instructor_constraint.sql
ALTER TABLE `credential_instructor`
  ADD UNIQUE KEY `UK_credential_instructor_user_credential` (user, credential);

#v0.7_deleting_old_tables_from_0.6.sql
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE annotation;
DROP TABLE badge;
DROP TABLE competence_activity;
DROP TABLE course;
DROP TABLE course_basedon_course;
DROP TABLE course_blogs;
DROP TABLE course_competence;
DROP TABLE course_enrollment;
DROP TABLE course_enrollment_added_competences_course_competence;
DROP TABLE course_excluded_feed_sources;
DROP TABLE course_hashtags;
DROP TABLE course_instructor;
DROP TABLE course_portfolio;
DROP TABLE course_portfolio_enrollments_course_enrollment;
DROP TABLE course_tags;
DROP TABLE completed_resource;
DROP TABLE evaluation;
DROP TABLE evaluation_badges;
DROP TABLE evaluation_submission;
DROP TABLE evaluation_submission_evaluations;
DROP TABLE event_reminder_guests;
DROP TABLE featured_news;
DROP TABLE featured_news_inbox;
DROP TABLE featured_news_inbox_user_featured_news;
DROP TABLE learning_goal_basedon_learning_goal;
DROP TABLE node;
DROP TABLE node_corequisites;
DROP TABLE node_hashtags;
DROP TABLE node_outcomes;
DROP TABLE node_prerequisites;
DROP TABLE node_tags;
DROP TABLE node_target_activities;
DROP TABLE notification;
DROP TABLE personal_calendar;
DROP TABLE personal_calendar_notifications;
DROP TABLE personal_calendar_reminders;
DROP TABLE portfolio;
DROP TABLE portfolio_external_credit_competences;
DROP TABLE portfolio_external_credit_target_activities;
DROP TABLE portfolio_portfolio_achieved_competence;
DROP TABLE portfolio_portfolio_completed_goal;
DROP TABLE portfolio_portfolio_external_credits;
DROP TABLE post;
DROP TABLE post_hashtags;
DROP TABLE post_mentioned_users;
DROP TABLE recommendation;
DROP TABLE reminder;
DROP TABLE request;
DROP TABLE rich_content;
DROP TABLE social_activity;
DROP TABLE social_activity_hashtags;
DROP TABLE social_activity_comments;
DROP TABLE social_activity_notification;
DROP TABLE social_activity_notification_sub_views;
DROP TABLE social_stream_sub_view;
DROP TABLE social_stream_sub_view_hashtags;
DROP TABLE social_stream_sub_view_related_resources;
DROP TABLE user_featured_news;
DROP TABLE user_learning_goals;
DROP TABLE user_learning_goal_target_competence;
DROP TABLE user_notification_actions;
ALTER TABLE followed_entity DROP FOREIGN KEY FK_e621cferbujqxfxjau0o4mfq6 ;
ALTER TABLE followed_entity DROP INDEX FK_e621cferbujqxfxjau0o4mfq6;
ALTER TABLE followed_entity DROP COLUMN followed_node;
SET FOREIGN_KEY_CHECKS = 1;

