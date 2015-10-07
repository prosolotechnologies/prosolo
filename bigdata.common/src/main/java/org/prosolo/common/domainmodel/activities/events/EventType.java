package org.prosolo.common.domainmodel.activities.events;

public enum EventType {
	AcceptRecommendation, 
	Assessment, 
	ChangeVisibility, 
	Like,
	RemoveLike,
	RemoveDislike,
	Dislike,
	Post,
	TwitterPost,
	AddNote,
	PostShare,
	Comment, 
	Tag, 
	Attach,
	AttachAll,
	Detach,
	Edit, 
	Edit_Profile, 
	Create,
	Create_Manager,
	Delete,
	Follow,
	Unfollow,
	Registered,
	MarkAsFavourite, 
	FollowEvent, 
	Create_recommendation, 
	DISMISS_RECOMMENDATION,
	Completion, 
	NotCompleted,
	GiveRecognition, 
	StatusUpdate,
	ChangeProgress, 
	RequestSent,
	Event,
	CommentsEnabled,
	CommentsDisabled,
	
	UpdatedSocialNetworks,
	
	// activities
	AssignmentRemoved,
	
	// content types
	FileUploaded,
	LinkAdded,
	
	// requests
	JoinedGoal,
	JOIN_GOAL_REQUEST,
	JOIN_GOAL_REQUEST_APPROVED,
	JOIN_GOAL_REQUEST_DENIED,
	JOIN_GOAL_REQUEST_IGNORED,
	JOIN_GOAL_INVITATION,
	JOIN_GOAL_INVITATION_ACCEPTED,
	EVALUATION_REQUEST,
	
	// evaluations
	EVALUATION_ACCEPTED,
	EVALUATION_GIVEN,
//	EVALUATION_EDITED,
	
	SEND_MESSAGE,
	START_MESSAGE_THREAD, 
	UPDATE_MESSAGE_THREAD,
	
	ENROLL_COURSE,
	COURSE_WITHDRAWN,
	COURSE_COMPLETED, 
	
	NAVIGATE, SERVICEUSE, LOGIN, SESSIONENDED, LOGOUT, 
	
	FILTER_CHANGE, HIDE_SOCIAL_ACTIVITY, UNFOLLOW_HASHTAGS,
	
	CREATE_PERSONAL_SCHEDULE, 
	CREATE_PERSONAL_SCHEDULE_ACCEPTED, 
	SELECT_GOAL, 
	SELECT_COMPETENCE, 
	GOAL_WALL_FILTER, 
	COMPETENCES_COMPARE, 
	ACTIVITIES_COMPARE, 
	NEW_COMPETENCE_DIALOG, 
	INTERESTS_UPDATED, 
	SEND_TO_LEARN, 
	ACTIVATE_COURSE, 
	ACTIVITY_REPORT_AVAILABLE, 
	MENTIONED, UPDATE_HASHTAGS, UPDATE_TAGS, PostUpdate,
	ARCHIVE_GOAL
	;
	
	private String customText;
	
	private EventType(String customText) {
		this.customText = customText;
	}
	    
	private EventType() { }
	
	public String getCustomText() {
		return customText;
	}

	public EventType setCustomText(String customText){
		this.customText = customText;
		return this;
	}
}
