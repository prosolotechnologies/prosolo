package org.prosolo.services.notifications.eventprocessing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CompetenceAssessmentRequestAcceptEventProcessor extends CompetenceAssessmentStatusChangeByAssessorEventProcessor {

	public CompetenceAssessmentRequestAcceptEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                           NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                           AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
	}

    @Override
    NotificationType getNotificationType() {
        return NotificationType.ASSESSMENT_REQUEST_ACCEPTED;
    }

}
