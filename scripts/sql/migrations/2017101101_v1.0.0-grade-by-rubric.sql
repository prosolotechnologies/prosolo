CREATE TABLE `category_assessment` (
    `id` bigint(20) NOT NULL PRIMARY KEY,
    `created` datetime DEFAULT NULL,
    `deleted` char(1) DEFAULT 'F',
    `description` longtext,
    `title` varchar(255) DEFAULT NULL,
    `assessment` bigint(20) NOT NULL,
    `category` bigint(20) NOT NULL,
    `level` bigint(20) NOT NULL,
    `comment` varchar(255) DEFAULT NULL
);

ALTER TABLE `category_assessment`
  ADD CONSTRAINT `FK_category_assessment_assessment` FOREIGN KEY (`assessment`) REFERENCES `activity_assessment` (`id`),
  ADD CONSTRAINT `FK_category_assessment_category` FOREIGN KEY (`category`) REFERENCES `category` (`id`),
  ADD CONSTRAINT `FK_category_assessment_level` FOREIGN KEY (`level`) REFERENCES `level` (`id`),
  ADD UNIQUE KEY `UK_category_assessment_assessment_category` (assessment, category);