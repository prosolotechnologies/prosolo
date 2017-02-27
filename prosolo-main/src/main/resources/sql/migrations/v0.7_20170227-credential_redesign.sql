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
	ADD CONSTRAINT `FK_deliveryOf` FOREIGN KEY (`delivery_of`) REFERENCES `credential1` (`id`);

ALTER TABLE `competence1` 
	ADD COLUMN `original_version` bigint(20),
	ADD COLUMN `date_published` datetime DEFAULT NULL,
	ADD COLUMN `can_be_edited` char(1) DEFAULT 'F',
	ADD CONSTRAINT `FK_originalVersion` FOREIGN KEY (`original_version`) REFERENCES `competence1` (`id`);
	
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
	ADD UNIQUE KEY `UK_competence_user` (`competence`,`user`);

DROP TABLE `target_activity1_links`;
DROP TABLE `target_activity1_files`;
DROP TABLE `target_activity1_captions`;

ALTER TABLE `target_activity1`
	DROP FOREIGN KEY `FK_7sb8h5hca0bkw3smwl4e2pq3r`;
	
ALTER TABLE `target_activity1`
	DROP COLUMN `act_order`,
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
