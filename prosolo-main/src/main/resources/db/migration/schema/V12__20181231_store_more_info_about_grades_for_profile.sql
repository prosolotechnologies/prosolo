ALTER TABLE `student_profile_config`
  DROP COLUMN `grade`,
  DROP COLUMN `max_grade`,
  ADD COLUMN `grade_type` varchar(255) NOT NULL DEFAULT 'NO_GRADE',
  ADD COLUMN `points_achieved` int(11),
  ADD COLUMN `max_points` int(11),
  ADD COLUMN `avg_level_achieved` int(11),
  ADD COLUMN `number_of_levels` int(11);
