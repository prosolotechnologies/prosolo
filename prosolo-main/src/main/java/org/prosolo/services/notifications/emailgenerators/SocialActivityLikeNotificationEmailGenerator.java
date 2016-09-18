package org.prosolo.services.notifications.emailgenerators;

public class SocialActivityLikeNotificationEmailGenerator extends NotificationEmailGenerator {
	
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-social-activity-liked";
	}
	
	@Override
	public String getSubject() {
		return "Someone liked your post";
	}

	public SocialActivityLikeNotificationEmailGenerator(String name, String actor, String predicate,
			String date, String link) {
		super(name, actor, predicate, null, date, link);
	}

}
