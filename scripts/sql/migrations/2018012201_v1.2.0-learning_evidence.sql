CREATE TABLE `credential_assessment_config` (
  `id` bigint(20) NOT NULL PRIMARY KEY,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `credential` bigint(20) NOT NULL,
  `assessment_type` varchar(255) NOT NULL,
  `enabled` char(1) DEFAULT 'F'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `credential_assessment_config`
  ADD CONSTRAINT `FK_credential_assessment_config_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`),
  ADD UNIQUE KEY `UK_credential_assessment_config_credential_assessment_type` (`credential`, `assessment_type`);

ALTER TABLE `credential1`
  ADD COLUMN `max_points` int(11) NOT NULL DEFAULT 0,
  ADD COLUMN `grading_mode` varchar(255) NOT NULL DEFAULT 'AUTOMATIC',
  ADD COLUMN `rubric` bigint(20),
  ADD CONSTRAINT `FK_credential_rubric` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`);

SET @t1=0;
INSERT INTO `credential_assessment_config`(`id`, `deleted`, `credential`, `assessment_type`, `enabled`)
   SELECT @t1 := @t1+1, 'F', c.id, 'INSTRUCTOR_ASSESSMENT', 'T'
   FROM credential1 c;

INSERT INTO `credential_assessment_config`(`id`, `deleted`, `credential`, `assessment_type`, `enabled`)
  SELECT @t1 := @t1+1, 'F', c.id, 'SELF_ASSESSMENT', 'F'
  FROM credential1 c;

INSERT INTO `credential_assessment_config`(`id`, `deleted`, `credential`, `assessment_type`, `enabled`)
  SELECT @t1 := @t1+1, 'F', c.id, 'PEER_ASSESSMENT', 'F'
  FROM credential1 c;

INSERT INTO `hibernate_sequences` VALUES ('credential_assessment_config', 2);

CREATE TABLE `competence_assessment_config` (
  `id` bigint(20) NOT NULL PRIMARY KEY,
  `created` datetime DEFAULT NULL,
  `deleted` char(1) DEFAULT NULL,
  `description` longtext,
  `title` varchar(255) DEFAULT NULL,
  `competence` bigint(20) NOT NULL,
  `assessment_type` varchar(255) NOT NULL,
  `enabled` char(1) DEFAULT 'F'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `competence_assessment_config`
  ADD CONSTRAINT `FK_competence_assessment_config_competence` FOREIGN KEY (`competence`) REFERENCES `competence1` (`id`),
  ADD UNIQUE KEY `UK_competence_assessment_config_competence_assessment_type` (`competence`, `assessment_type`);

ALTER TABLE `competence1`
  ADD COLUMN `max_points` int(11) NOT NULL DEFAULT 0,
  ADD COLUMN `grading_mode` varchar(255) NOT NULL DEFAULT 'AUTOMATIC',
  ADD COLUMN `rubric` bigint(20),
  ADD CONSTRAINT `FK_competence1_rubric` FOREIGN KEY (`rubric`) REFERENCES `rubric` (`id`);

SET @t1=0;
INSERT INTO `competence_assessment_config`(`id`, `deleted`, `competence`, `assessment_type`, `enabled`)
  SELECT @t1 := @t1+1, 'F', c.id, 'INSTRUCTOR_ASSESSMENT', 'T'
  FROM competence1 c;

INSERT INTO `competence_assessment_config`(`id`, `deleted`, `competence`, `assessment_type`, `enabled`)
  SELECT @t1 := @t1+1, 'F', c.id, 'SELF_ASSESSMENT', 'F'
  FROM competence1 c;

INSERT INTO `competence_assessment_config`(`id`, `deleted`, `competence`, `assessment_type`, `enabled`)
  SELECT @t1 := @t1+1, 'F', c.id, 'PEER_ASSESSMENT', 'F'
  FROM competence1 c;

INSERT INTO `hibernate_sequences` VALUES ('competence_assessment_config', 2);