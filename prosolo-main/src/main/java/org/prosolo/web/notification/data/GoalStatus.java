package org.prosolo.web.notification.data;

public enum GoalStatus {
	HAS_ACTIVE_COURSE_BASED_GOAL, 		// goal is connected to a course and user is actively enrolled into the course
	HAS_INACTIVE_COURSE_BASED_GOAL, 	// goal is connected to a course, but user has withdrawn from it. Learning experience (TargetLearningGoal) is saved
	NO_GOAL_COURSE_CONNECTED, 			// goal is connected to a course, but user never enrolled it
	HAS_GOAL_REGULAR,					// this is a regular goal (not course-connected), and user is enrolled in it
	NO_GOAL_REGULAR,					// this is a regular goal (not course-connected), but user never enrolled it
}
