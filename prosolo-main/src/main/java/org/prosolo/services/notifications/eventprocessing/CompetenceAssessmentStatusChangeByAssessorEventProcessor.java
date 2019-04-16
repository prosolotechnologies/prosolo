package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public abstract class CompetenceAssessmentStatusChangeByAssessorEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentStatusChangeByAssessorEventProcessor.class);

	public CompetenceAssessmentStatusChangeByAssessorEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                                    ContextJsonParserService ctxJsonParserService,
                                                                    CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, ctxJsonParserService, credentialManager, competenceManager);
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
