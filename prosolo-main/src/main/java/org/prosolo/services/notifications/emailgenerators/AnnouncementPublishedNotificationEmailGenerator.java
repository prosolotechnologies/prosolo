package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public class AnnouncementPublishedNotificationEmailGenerator extends NotificationEmailGenerator {
	
	private String subject;
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-announcement";
	}
	
	@Override
	public String getSubject() {
		return subject;
	}

	public AnnouncementPublishedNotificationEmailGenerator(String name, String actor, String predicate, String objectTitle,
			String date, String link, NotificationType type) {
		super(name, actor, predicate, objectTitle, date, link);
		this.subject = "Announcement for " + objectTitle;
	}
	
}
