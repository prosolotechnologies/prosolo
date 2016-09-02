package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public class AnnouncementPublishedNotificationEmailGenerator extends NotificationEmailGenerator {
	
	private String subject;
	private String targetTitle;
	private String text;
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-announcement";
	}
	
	@Override
	public String getSubject() {
		return subject;
	}

	public AnnouncementPublishedNotificationEmailGenerator(String name, String actor, String objectTitle, String credentialTargetTitle, String predicate,
			String date, String link, NotificationType type, String text) {
		super(name, actor, predicate, objectTitle, date, link);
		this.subject = "Announcement for " + credentialTargetTitle;
		this.targetTitle = credentialTargetTitle;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public String getTargetTitle() {
		return targetTitle;
	}
	
}
