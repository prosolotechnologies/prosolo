ALTER TABLE `credential1`
    ADD COLUMN `organization` bigint(20) DEFAULT NULL,
    ADD CONSTRAINT `FK_credential_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);

ALTER TABLE `competence1`
    ADD COLUMN `organization` bigint(20) DEFAULT NULL,
    ADD CONSTRAINT `FK_competence_organization` FOREIGN KEY (`organization`) REFERENCES `organization` (`id`);