package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class AssessmentNotificationEventProcessor extends SimpleNotificationEventProcessor {

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
		return getSenderData(event.getActorId());
	}

	public NotificationSenderData getSenderData(long actorId) {
		return new NotificationSenderData(actorId, getUserRole(actorId));
	}

	private boolean isAnonymized(long userId) {
		NotificationActorRole userRole = getUserRole(userId);
		BlindAssessmentMode blindAssessmentMode = getBlindAssessmentMode();
		if ((blindAssessmentMode == BlindAssessmentMode.BLIND && userRole == NotificationActorRole.ASSESSOR)
				|| (blindAssessmentMode == BlindAssessmentMode.DOUBLE_BLIND && (userRole == NotificationActorRole.ASSESSOR || userRole == NotificationActorRole.STUDENT))) {
			return true;
		}
		return false;
	}

	private NotificationActorRole getUserRole(long userId) {
		long assessorId = getAssessorId();
		long studentId = getStudentId();
		return userId != 0 && userId == assessorId
					? NotificationActorRole.ASSESSOR
					: userId != 0 && userId == studentId
						? NotificationActorRole.STUDENT
						: NotificationActorRole.OTHER;
	}


	@Override
	boolean isAnonymizedActor() {
		return isAnonymized(getSenderData().getSenderId())
				|| (getObjectType() == ResourceType.Student && isAnonymized(getObjectId()))
				|| (getTargetType() == ResourceType.Student && isAnonymized(getTargetId()));
	}
}
