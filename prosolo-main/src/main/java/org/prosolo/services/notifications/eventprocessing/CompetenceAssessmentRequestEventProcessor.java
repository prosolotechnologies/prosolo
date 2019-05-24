package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CompetenceAssessmentRequestEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CompetenceAssessmentRequestEventProcessor.class);

    private AssessmentManager assessmentManager;

	public CompetenceAssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, AssessmentManager assessmentManager) {
		super(event, event.getObject().getId(), session, notificationManager, notificationsSettingsManager, idEncoder);
		this.assessmentManager = assessmentManager;
	}

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        PageSection section = getAssessment().getType() == AssessmentType.INSTRUCTOR_ASSESSMENT ? PageSection.MANAGE : PageSection.STUDENT;
        List<NotificationReceiverData> receivers = new ArrayList<>();
        receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(section),
                false, section));
        return receivers;
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.Assessment_Requested;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Competence;
    }

    @Override
    long getObjectId() {
        return getAssessment().getCompetence().getId();
    }

    private String getNotificationLink(PageSection section) {
        return AssessmentLinkUtil.getAssessmentNotificationLink(
                getContext(), getCredentialId(), getAssessment().getCompetence().getId(), getAssessment().getId(), getAssessment().getType(), assessmentManager, idEncoder, session, section);
    }

}
