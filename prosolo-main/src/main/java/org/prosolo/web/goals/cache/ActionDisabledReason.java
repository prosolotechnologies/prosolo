package org.prosolo.web.goals.cache;

public enum ActionDisabledReason {
	
	NONE,
	NONE_CHECK,
	NONE_UNCHECK,

	/* 
	 * Used when manual marking of a competence as completed is disabled
	 * due to the existence of an upload activity that is not completed
	 */
	COMPLETION_DISABLED_UPLOAD_ACTIVITY_LEFT_UNCOMPLETED,
	
	/* 
	 * Competences that are predefined for a course can not be deleted from
	 * a course-based goal
	 */
	DELETION_DISABLED_COMPETENCE_PREDEFINED_FOR_COURSE,
	
	/*
	 * Manual marking of an activity as completed is disabled for UploadAssignmentActivity
	 */
	COMPLETION_DISABLED_FOR_UPLOAD_ACTIVITY,
	COMPLETION_DISABLED_FOR_UPLOAD_ACTIVITY_CHECK,
	COMPLETION_DISABLED_FOR_UPLOAD_ACTIVITY_UNCHECK,
	
}
