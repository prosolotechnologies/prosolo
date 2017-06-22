package org.prosolo.common.domainmodel.events;

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
	Comment_Reply,
	Tag, 
	Attach,
	AttachAll,
	Detach,
	Edit, 
	Edit_Profile, 
	Edit_Draft,
	View_Profile, 
	Create,
	Create_Manager,
	Create_Draft,
	Delete,
	Delete_Draft,
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
	AssignmentUploaded,
	
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
	READ_MESSAGE_THREAD, 
	UPDATE_MESSAGE_THREAD,
	
	ENROLL_COURSE,
	ENROLL_COMPETENCE,
	COURSE_WITHDRAWN,
	CREDENTIAL_COMPLETED, 
	
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
	HASHTAG_ENABLED, HASHTAG_DISABLED, ARCHIVE_GOAL,
	DIGEST_FILTER_UPDATED,
	
	//this activity exists in MOOC logs for some reasons
	HIDE_ACTIVITY,
	COMMENT_CANCEL,
	
	INSTRUCTOR_ASSIGNED_TO_CREDENTIAL,
	INSTRUCTOR_REMOVED_FROM_CREDENTIAL,
	STUDENT_ASSIGNED_TO_INSTRUCTOR,
	STUDENT_UNASSIGNED_FROM_INSTRUCTOR,
	STUDENT_REASSIGNED_TO_INSTRUCTOR,
	
	USER_ROLES_UPDATED,
	
	Bookmark,
	RemoveBookmark, 
	AssessmentRequested, 
	AssessmentApproved,
	AssessmentComment, 
	AnnouncementPublished,
	
	Typed_Response_Posted,
	Typed_Response_Edit,
	
	SCHEDULED_VISIBILITY_UPDATE,
	CANCEL_SCHEDULED_VISIBILITY_UPDATE,
	ADD_USER_TO_GROUP,
	REMOVE_USER_FROM_GROUP,
	RESOURCE_VISIBILITY_CHANGE,
	USER_GROUP_ADDED_TO_RESOURCE,
	USER_GROUP_REMOVED_FROM_RESOURCE,
	//RESOURCE_USER_GROUP_PRIVILEGE_CHANGE,
	//users are added and/or removed to the group
	USER_GROUP_CHANGE,
	VISIBLE_TO_ALL_CHANGED,
	STATUS_CHANGED,
	ADD_INSTRUCTOR_TO_CREDENTIAL,
	REMOVE_INSTRUCTOR_FROM_CREDENTIAL,
	GRADE_ADDED,
	
	PAGE_OPENED,
	
	ARCHIVE,
	RESTORE,

	OWNER_CHANGE;
	
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