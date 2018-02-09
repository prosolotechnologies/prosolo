package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class AssessmentApprovedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(AssessmentApprovedEventProcessor.class);

	public AssessmentApprovedEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		try {
			receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(), false, PageSection.STUDENT));
			return receivers;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Approved;
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
		return "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(event.getObject().getId());
	}

}
