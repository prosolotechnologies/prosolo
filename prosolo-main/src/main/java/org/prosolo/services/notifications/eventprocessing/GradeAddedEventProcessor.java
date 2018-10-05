package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public abstract class GradeAddedEventProcessor extends AssessmentNotificationEventProcessor {

	private static Logger logger = Logger.getLogger(GradeAddedEventProcessor.class);

	public GradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return sender != receiver;
	}

	protected abstract boolean shouldNotificationBeGenerated();
	protected abstract long getStudentId();
	protected abstract long getAssessorId();
	protected abstract String getNotificationLink();


	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();

		try {
			// this notification should be created only if it is not self-assessment
			if (shouldNotificationBeGenerated()) {
				receivers.add(new NotificationReceiverData(getStudentId(), getNotificationLink(), false, PageSection.STUDENT));
			}
			return receivers;
		} catch (Exception e) {
			logger.error("Error", e);
			return new ArrayList<>();
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.GradeAdded;
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}

}
