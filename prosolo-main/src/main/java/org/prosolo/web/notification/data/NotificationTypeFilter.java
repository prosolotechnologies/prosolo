package org.prosolo.web.notification.data;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public enum NotificationTypeFilter {

	Follow_User(NotificationType.Follow_User, "Followers"),
	Comment(NotificationType.Comment, "Comments"),
	Comment_Like(NotificationType.Comment_Like, "Comment Likes"),
	Mention(NotificationType.Mention, "Mentions"),
	Assessment_Given(NotificationType.Assessment_Given, "Assessments"),
	Started_Learning(NotificationType.Started_Learning, "Start Learning");
	
	private NotificationType type;
	private String label;
	
	private NotificationTypeFilter(NotificationType type, String label) {
		this.type = type;
		this.label = label;
	}

	public NotificationType getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}
	
}
