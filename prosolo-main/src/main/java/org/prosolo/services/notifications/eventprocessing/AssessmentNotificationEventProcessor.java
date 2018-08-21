package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class AssessmentNotificationEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(AssessmentNotificationEventProcessor.class);

	public AssessmentNotificationEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	protected abstract BlindAssessmentMode getBlindAssessmentMode();
	protected abstract long getAssessorId();
	protected abstract long getStudentId();

	@Override
	NotificationSenderData getSenderData() {
		BlindAssessmentMode blindAssessmentMode = getBlindAssessmentMode();
		long assessorId = getAssessorId();
		long studentId = getStudentId();
		NotificationActorRole actorRole = event.getActorId() == assessorId
				? NotificationActorRole.ASSESSOR
				: event.getActorId() == studentId
					? NotificationActorRole.STUDENT
					: NotificationActorRole.OTHER;
		boolean anonymizedActor = false;
		if ((blindAssessmentMode == BlindAssessmentMode.BLIND && actorRole == NotificationActorRole.ASSESSOR)
				|| (blindAssessmentMode == BlindAssessmentMode.DOUBLE_BLIND && (actorRole == NotificationActorRole.ASSESSOR || actorRole == NotificationActorRole.STUDENT))) {
			anonymizedActor = true;
		}
		return new NotificationSenderData(event.getActorId(), actorRole, anonymizedActor);
	}

}
