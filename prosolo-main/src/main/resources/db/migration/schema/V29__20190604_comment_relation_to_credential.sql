ALTER TABLE comment1
    ADD COLUMN `credential` bigint(20) DEFAULT NULL;

ALTER TABLE `comment1`
    ADD CONSTRAINT `FK_comment1_credential` FOREIGN KEY (`credential`) REFERENCES `credential1` (`id`);