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