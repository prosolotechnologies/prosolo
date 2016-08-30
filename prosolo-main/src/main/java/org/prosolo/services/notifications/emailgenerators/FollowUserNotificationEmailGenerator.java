package org.prosolo.services.notifications.emailgenerators;

public class FollowUserNotificationEmailGenerator extends NotificationEmailGenerator {
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-followed";
	}
	
	@Override
	public String getSubject() {
		return "You have a new follower on ProSolo";
	}

	public FollowUserNotificationEmailGenerator(String name, String actor, String predicate, String objectTitle,
			String date, String link) {
		super(name, actor, predicate, objectTitle, date, link);
	}

}
