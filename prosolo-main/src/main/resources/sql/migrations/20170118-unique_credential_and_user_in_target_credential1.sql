ALTER TABLE `target_credential1`
  DROP INDEX `FK_s3fmqrd1ct10kj04au40cuqhr`,
  ADD UNIQUE KEY `UK_hxf01pvgri60h660un28e1w2q` (`credential`,`user`);