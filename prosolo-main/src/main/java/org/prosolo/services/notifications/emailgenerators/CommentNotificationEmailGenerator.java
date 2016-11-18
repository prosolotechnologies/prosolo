package org.prosolo.services.notifications.emailgenerators;

public class CommentNotificationEmailGenerator extends NotificationEmailGenerator {
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-commented";
	}
	
	@Override
	public String getSubject() {
		return "There is a new comment for you";
	}

	public CommentNotificationEmailGenerator(String name, String actor, String predicate, String objectTitle,
			String date, String link) {
		super(name, actor, predicate, objectTitle, date, link);
	}

}
