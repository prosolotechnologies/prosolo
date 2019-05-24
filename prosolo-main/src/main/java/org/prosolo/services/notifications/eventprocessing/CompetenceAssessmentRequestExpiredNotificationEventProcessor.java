package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-05-14
 * @since 1.3.2
 */
public class CompetenceAssessmentRequestExpiredNotificationEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentRequestExpiredNotificationEventProcessor.class);

	public CompetenceAssessmentRequestExpiredNotificationEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                                        NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, event.getObject().getId(), session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		receivers.add(new NotificationReceiverData(getStudentId(), getNotificationLink(),
				false, PageSection.STUDENT));
		return receivers;
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.ASSESSMENT_REQUEST_EXPIRED;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Competence;
	}

	@Override
	long getObjectId() {
		return getAssessment().getCompetence().getId();
	}

	private String getNotificationLink() {
		return AssessmentLinkUtil.getCompetenceAssessmentNotificationLinkForStudent(
				getCredentialId(), getAssessment().getCompetence().getId(), getAssessment().getId(), getAssessment().getType(), idEncoder);
	}
}
