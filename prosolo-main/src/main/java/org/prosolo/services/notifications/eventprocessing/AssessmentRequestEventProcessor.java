package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class AssessmentRequestEventProcessor extends NotificationEventProcessor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AssessmentRequestEventProcessor.class);
	
	public AssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
										   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, UserManager userManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, userManager);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(), 
				false, PageSection.STUDENT));
		return receivers;
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Requested;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Credential;
	}

	@Override
	long getObjectId() {
		return Long.parseLong(event.getParameters().get("credentialId"));
	}

	private String getNotificationLink() {
		//assessment request can be made only to regular users - students - peers
		return "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(event.getObject().getId());
	}

}
