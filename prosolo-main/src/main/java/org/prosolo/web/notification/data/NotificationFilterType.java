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
	Social_Activity_Like(NotificationType.Social_Activity_Like, "Post Likes")
	;

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
