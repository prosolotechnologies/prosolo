package org.prosolo.web.notification.data;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public enum NotificationFilterType {

	Follow_User(NotificationType.Follow_User, "Followers"),
	Comment(NotificationType.Comment, "Comments"),
	Comment_Like(NotificationType.Comment_Like, "Comment Likes"),
	Mention(NotificationType.Mention, "Mentions"),
	Assessment_Approved(NotificationType.Assessment_Approved, "Assessments Submitted"),
	Assessment_Requested(NotificationType.Assessment_Requested, "Assessments Requested"),
	Assessment_Comment(NotificationType.Assessment_Comment, "Assessment Comments"),
	GradeAdded(NotificationType.GradeAdded, "Assessments Updated"),
	Announcement_Published(NotificationType.AnnouncementPublished, "Announcements"),
	Social_Activity_Like(NotificationType.Social_Activity_Like, "Post Likes"),
	Assessment_Request_Accepted(NotificationType.ASSESSMENT_REQUEST_ACCEPTED, "Assessment requests accepted"),
	Assessment_Request_Declined(NotificationType.ASSESSMENT_REQUEST_DECLINED, "Assessment requests declined"),
	Assessor_Withdrew_From_Assessment(NotificationType.ASSESSOR_WITHDREW_FROM_ASSESSMENT, "Withdrawn assessments by assessor"),
	Assessor_Assigned_To_Assessment(NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT, "Assessors assigned to your assessment requests"),
	Assigned_To_Assessment_As_Assessor(NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR, "Assigned as assessor to assessment requests"),
	Assessment_Request_Expired(NotificationType.ASSESSMENT_REQUEST_EXPIRED, "Assessment requests expired"),
	Assessment_Tokens_Number_Updated(NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED, "Assessment tokens number updates");


	private NotificationType notificationType;
	private String label;

	NotificationFilterType(NotificationType notificationType, String label) {
		this.notificationType = notificationType;
		this.label = label;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}
	
	public String getLabel() {
		return label;
	}

}
