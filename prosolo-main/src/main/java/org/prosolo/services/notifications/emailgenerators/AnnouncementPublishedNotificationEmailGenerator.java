package org.prosolo.services.notifications.emailgenerators;

import org.prosolo.web.util.ResourceBundleUtil;

public class AnnouncementPublishedNotificationEmailGenerator extends NotificationEmailGenerator {
	
	private String subject;
	private String targetTitle;
	private String text;
	private String localizedCredentialLabel;
	
	@Override
	public String getTemplateName() {
		return "notifications/notification-announcement";
	}
	
	@Override
	public String getSubject() {
		return subject;
	}

	public AnnouncementPublishedNotificationEmailGenerator(String name, String actor, String objectTitle, String credentialTitle, String predicate,
			String date, String link, String announcementTitle, String announcementText) {
		super(name, actor, predicate, objectTitle, date, link);
		this.subject = "["+credentialTitle+"] " + announcementTitle;
		this.targetTitle = credentialTitle;
		this.text = announcementText;
		this.localizedCredentialLabel = ResourceBundleUtil.getLabel("label.credential").toLowerCase();
	}

	public String getText() {
		return text;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public String getLocalizedCredentialLabel() {
		return localizedCredentialLabel;
	}
}
