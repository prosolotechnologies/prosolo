package org.prosolo.services.event.context;

public enum ContextName {

	CREDENTIAL("Course"),
	GOAL_WALL(""),
	COMPETENCE_WALL("TargetCompetence"),
	ACTIVITY_WALL("TargetActivity"),
	POST_DIALOG("Post"),
	INSTRUCTOR("CourseInstructor"),
	INSTRUCTOR_DIALOG("CourseInstructor"),
	USER("User"),
	ACTIVITY_SEARCH("Activity"),
	NODE_COMMENT("NodeComment"),
	SOCIAL_ACTIVITY_COMMENT("SocialActivityComment"),
	LEARNING_GOAL("TargetLearningGoal"),
	SOCIAL_ACTIVITY("SocialActivity"),
	STATUS_WALL("");
	
    private String objectType; 
    
    ContextName(String objectType) {
        this.objectType = objectType;
    }

	public String getObjectType() {
		return objectType;
	}

}
