ALTER TABLE `rubric_unit`
     ADD KEY `FK_rubric_unit_rubric` (`rubric`),
     ADD KEY `FK_rubric_unit_unit` (`unit`);
