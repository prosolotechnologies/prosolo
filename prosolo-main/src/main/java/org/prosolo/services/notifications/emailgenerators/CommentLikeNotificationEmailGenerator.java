package org.prosolo.services.notifications.emailgenerators;

public class CommentLikeNotificationEmailGenerator extends NotificationEmailGenerator {
	
	private String targetType;
	private String targetTitle;
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-comment-liked";
	}
	
	@Override
	public String getSubject() {
		return "Someone liked your comment";
	}

	public CommentLikeNotificationEmailGenerator(String name, String actor, String predicate, String targetType, String targetTitle,
			String date, String link) {
		super(name, actor, predicate, null, date, link);
		this.targetType = targetType;
		this.targetTitle = targetTitle;
	}

	public String getTargetType() {
		return targetType;
	}

	public String getTargetTitle() {
		return targetTitle;
	}
	
}
