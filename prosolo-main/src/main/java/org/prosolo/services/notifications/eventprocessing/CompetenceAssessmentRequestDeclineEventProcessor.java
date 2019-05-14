package org.prosolo.services.notifications.eventprocessing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.event.Event;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CompetenceAssessmentRequestDeclineEventProcessor extends CompetenceAssessmentStatusChangeByAssessorEventProcessor {

	public CompetenceAssessmentRequestDeclineEventProcessor(Event event, Session session, NotificationManager notificationManager, NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, ContextJsonParserService ctxJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, ctxJsonParserService);
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.ASSESSMENT_REQUEST_DECLINED;
	}

}
