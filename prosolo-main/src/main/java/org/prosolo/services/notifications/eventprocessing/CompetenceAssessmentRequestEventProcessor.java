package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
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

	public CompetenceAssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                     AssessmentManager assessmentManager) {
		super(event, event.getObject().getId(), session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
	}

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        PageSection section = assessment.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT ? PageSection.MANAGE : PageSection.STUDENT;
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
        return assessment.getCompetence().getId();
    }

    private String getNotificationLink(PageSection section) {
        Context context = ContextJsonParserService.parseContext(event.getContext());

        AssessmentType type = assessment.getType();

        if (type == AssessmentType.INSTRUCTOR_ASSESSMENT) {
            long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);

            return AssessmentLinkUtil.getCredentialAssessmentPageLink(
                    credentialId,
                    credentialAssessmentId,
                    type,
                    idEncoder,
                    PageSection.MANAGE);
        } else if (type == AssessmentType.PEER_ASSESSMENT) {
            long competenceAssessmentId = assessment.getId();

            return section.getPrefix() +
                    "/assessments/my/competences/" + idEncoder.encodeId(competenceAssessmentId);
        } else {
            throw new IllegalArgumentException("Cannot generate notification link for page ");
        }
    }

}
