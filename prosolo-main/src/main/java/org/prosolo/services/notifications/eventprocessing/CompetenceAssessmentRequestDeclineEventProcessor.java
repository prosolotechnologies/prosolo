package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.event.Event;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class CompetenceAssessmentRequestDeclineEventProcessor extends CompetenceAssessmentStatusChangeByAssessorEventProcessor {

    public CompetenceAssessmentRequestDeclineEventProcessor(Event event, NotificationManager notificationManager,
                                                            NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                            AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.ASSESSMENT_REQUEST_DECLINED;
    }

}
