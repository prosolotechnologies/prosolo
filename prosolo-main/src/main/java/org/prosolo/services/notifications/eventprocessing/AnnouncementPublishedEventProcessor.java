package org.prosolo.services.notifications.eventprocessing;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.courses.credential.announcements.AnnouncementPublishMode;

public class AnnouncementPublishedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(AnnouncementPublishedEventProcessor.class);
	
	private CredentialManager credentialManager;

	public AnnouncementPublishedEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
			CredentialManager credentialManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.credentialManager = credentialManager;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<Long> getReceiverIds() {
		String publishMode = event.getParameters().get("publishMode");
		Long credId = Long.parseLong(event.getParameters().get("credentialId"));
		AnnouncementPublishMode mode = AnnouncementPublishMode.fromString(publishMode);
		
		if (mode == null) {
			logger.error(String.format("Publish mode for announcement was : %s, setting it to default : %s",
					publishMode, AnnouncementPublishMode.ALL_STUDENTS.getText()));
			mode = AnnouncementPublishMode.ALL_STUDENTS;
		}
		
		if (mode.equals(AnnouncementPublishMode.ALL_STUDENTS)) {
			return credentialManager.getUserIdsForCredential(credId);
		}
		else return credentialManager.getActiveUserIdsForCredential(credId);
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.AnnouncementPublished;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Announcement;
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}
	
	@Override
	public ResourceType getTargetType() {
		return ResourceType.Credential;
	}
	
	@Override
	public long getTargetId() {
		return event.getTarget().getId();
	}

	@Override
	String getNotificationLink() {
		return "/credentials/" + idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId")))
				+ "/announcements/" + idEncoder.encodeId(event.getObject().getId());
	}

}
