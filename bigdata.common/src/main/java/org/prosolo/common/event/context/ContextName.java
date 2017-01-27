package org.prosolo.common.event.context;

public enum ContextName {

	CREDENTIAL("Credential1"),
	GOAL_WALL(""),
	TARGET_COMPETENCE("TargetCompetence1"),
	TARGET_ACTIVITY("TargetActivity1"),
	POST_DIALOG("Post"),
	INSTRUCTOR("CourseInstructor"),
	INSTRUCTOR_DIALOG("CourseInstructor"),
	USER("User"),
	ACTIVITY_SEARCH("Activity"),
	NODE_COMMENT("NodeComment"),
	SOCIAL_ACTIVITY_COMMENT("SocialActivityComment"),
	//LEARNING_GOAL("TargetLearningGoal"),
	SOCIAL_ACTIVITY("SocialActivity1"),
	STATUS_WALL(""),
	NEW_POST(""),
	RESUME_LEARNING(""),
	COMPETENCE_PROGRESS(""),
	BREADCRUMBS(""),
	ASSESSMENT_DIALOG(""),
	ACTIVATE_COURSE_DIALOG(""),
	WITHDRAW_COURSE_DIALOG(""),
	ASSESSMENT(""),
	LTI_LAUNCH(""),
	LTI_TOOL("LtiTool"),
	ADD_ACTIVITY_DIALOG("Activity"),
	COMPETENCE("Competence1"),
	ACTIVITY_SEARCH_BOX("Activity"),
	DELETE_COMPETENCE_ACTIVITY_DIALOG("CompetenceActivity"),
	PERSONAL_FEEDS("FeedEntry"), 
	FRIENDS_FEEDS("FeedEntry"), 
	COURSE_FEEDS("FeedEntry"), 
	PERSONAL_TWEETS("TwitterPostSocialActivity"), 
	COURSE_TWEETS("TwitterPostSocialActivity"),
	NEWS_DIGEST("FeedsDigest"),
	COMMENT("Comment1"),
	LIBRARY(""),
	ACTIVITY("Activity1"),
	TARGET_CREDENTIAL("TargetCredential1"),
	ACTIVITY_LINK("ResourceLink"),
	ACTIVITY_FILE("ResourceLink"),
	DELETE_DIALOG(""),
	PROFILE("User"),
	WHO_TO_FOLLOW("User"),
	POST_SHARE_DIALOG(""),
	PREVIEW(""),
	NEW_COMPETENCE(""),
	NEW_ACTIVITY(""),
	SETTINGS_PERSONAL("User"),
	SETTINGS_EMAIL("User"),
	SETTINGS_PASSWORD("User"),
	MESSAGES(""),
	UPLOAD_RESULT_DIALOG(""),
	RESULT("TargetActivity1"),
	RESULTS(""),
	RESULT_PRIVATE_CONVERSATION_DIALOG("TargetActivity1"),
	STUDENTS(""),
	EDIT_DIALOG(""),
	USER_GROUPS_DIALOG("User"),
	JOIN_BY_URL_GROUP_DIALOG(""),
	MANAGE_VISIBILITY_DIALOG(""),
	PUBLISH_RESOURCE_DIALOG(""),
	ACTIVITY_ASSESSMENT("ActivityAssessment"),
	ACTIVITY_GRADE_DIALOG(""),
	ASSESSMENT_COMMENTS(""),
	ASK_FOR_ASSESSMENT_DIALOG(""),
	EXTERNAL_ACTIVITY_GRADE("TargetActivity1");
	
    private String objectType; 
    
    ContextName(String objectType) {
        this.objectType = objectType;
    }

	public String getObjectType() {
		return objectType;
	}

}
