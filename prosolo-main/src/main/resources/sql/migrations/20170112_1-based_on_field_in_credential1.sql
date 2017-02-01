ALTER TABLE `credential1`
  ADD COLUMN `based_on` bigint(20) DEFAULT NULL,
  ADD KEY `FK_hkwb5xu2110cpd8k6t0cnb4si` (`based_on`),
  ADD CONSTRAINT `FK_hkwb5xu2110cpd8k6t0cnb4si` FOREIGN KEY (`based_on`) REFERENCES `credential1` (`id`);