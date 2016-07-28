package org.prosolo.services.notifications.emailgenerators;

public class MentionNotificationEmailGenerator extends NotificationEmailGenerator {
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-mentioned";
	}
	
	@Override
	public String getSubject() {
		return "Someone mentioned you on ProSolo";
	}

	public MentionNotificationEmailGenerator(String name, String actor, String predicate, String objectTitle,
			String date, String link) {
		super(name, actor, predicate, objectTitle, date, link);
	}

}
