package org.prosolo.services.event.context;

public enum ContextName {

	CREDENTIAL("Course"),
	GOAL_WALL("TargetLearningGoal"),
	COMPETENCE_WALL("TargetCompetence"),
	ACTIVITY_WALL("TargetActivity"),
	POST_DIALOG("Post"),
	INSTRUCTOR("CourseInstructor"),
	INSTRUCTOR_DIALOG("CourseInstructor"),
	USER("User");
	
    private String objectType; 
    
    ContextName(String objectType) {
        this.objectType = objectType;
    }

	public String getObjectType() {
		return objectType;
	}

}
