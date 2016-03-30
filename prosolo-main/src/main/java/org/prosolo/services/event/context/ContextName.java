package org.prosolo.services.event.context;

public enum ContextName {

	CREDENTIAL("Credential"),
	GOAL_WALL(""),
	COMPETENCE("TargetCompetence"),
	ACTIVITY("TargetActivity"),
	POST_DIALOG("Post"),
	INSTRUCTOR("CourseInstructor"),
	INSTRUCTOR_DIALOG("CourseInstructor"),
	USER("User"),
	ACTIVITY_SEARCH("Activity"),
	NODE_COMMENT("NodeComment"),
	SOCIAL_ACTIVITY_COMMENT("SocialActivityComment"),
	LEARNING_GOAL("TargetLearningGoal"),
	SOCIAL_ACTIVITY("SocialActivity"),
	STATUS_WALL(""),
	ASSESSMENT_DIALOG(""),
	ACTIVATE_COURSE_DIALOG(""),
	WITHDRAW_COURSE_DIALOG(""),
	ASSESSMENT(""),
	LTI_LAUNCH(""),
	LTI_TOOL("LtiTool"),
	ADD_ACTIVITY_DIALOG("Activity"),
	BASE_COMPETENCE("Competence"),
	ACTIVITY_SEARCH_BOX("Activity"),
	DELETE_COMPETENCE_ACTIVITY_DIALOG("CompetenceActivity"),
	PERSONAL_FEEDS("FeedEntry"), 
	FRIENDS_FEEDS("FeedEntry"), 
	COURSE_FEEDS("FeedEntry"), 
	PERSONAL_TWEETS("TwitterPostSocialActivity"), 
	COURSE_TWEETS("TwitterPostSocialActivity"),
	NEWS_DIGEST("FeedsDigest");
	
	
    private String objectType; 
    
    ContextName(String objectType) {
        this.objectType = objectType;
    }

	public String getObjectType() {
		return objectType;
	}

}
