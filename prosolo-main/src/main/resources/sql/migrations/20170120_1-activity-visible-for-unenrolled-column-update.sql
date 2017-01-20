UPDATE `activity1` SET `visible_for_unenrolled_students` = 'F'
WHERE `visible_for_unenrolled_students` IS NULL;