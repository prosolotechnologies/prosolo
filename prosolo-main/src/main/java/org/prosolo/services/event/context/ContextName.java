package org.prosolo.services.event.context;

public enum ContextName {

	CREDENTIAL("Course"),
	GOAL_WALL("TargetLearningGoal"),
	COMPETENCE_WALL("TargetCompetence"),
	ACTIVITY_WALL("TargetActivity"),
	POST_DIALOG("Post");

    private String objectType; 
    
    ContextName(String objectType) {
        this.objectType = objectType;
    }

	public String getObjectType() {
		return objectType;
	}

}
