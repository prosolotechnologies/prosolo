package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.event.Event;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

/**
 * @author stefanvuckovic
 * @date 2019-05-09
 * @since 1.3.2
 */
public class AssessorAssignedToExistingCompetenceAssessmenEventProcessor extends ComplexNotificationEventProcessor {

    public AssessorAssignedToExistingCompetenceAssessmenEventProcessor(Event event, NotificationManager notificationManager,
                                                                       NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                                       AssessmentManager assessmentManager) {
        addNotificationEventProcessor(new AssessorAssignedToCompetenceAssessmentStudentNotificationEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager));
        addNotificationEventProcessor(new AssessorAssignedToCompetenceAssessmentAssessorNotificationEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager));
    }

}
