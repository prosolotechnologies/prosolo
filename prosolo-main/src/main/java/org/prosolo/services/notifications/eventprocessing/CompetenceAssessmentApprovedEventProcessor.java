package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CompetenceAssessmentApprovedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CompetenceAssessmentApprovedEventProcessor.class);

	private CompetenceAssessment competenceAssessment;
	private ContextJsonParserService contextJsonParserService;
	private AssessmentManager assessmentManager;

	public CompetenceAssessmentApprovedEventProcessor(Event event, Session session, NotificationManager notificationManager,
													  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													  AssessmentManager assessmentManager, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.contextJsonParserService = contextJsonParserService;
		this.assessmentManager = assessmentManager;
		this.competenceAssessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		// notification should not be sent in case of a self-assessment
		return sender != receiver;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		try {
			receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(competenceAssessment.getType()), false, PageSection.STUDENT));
			return receivers;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Approved;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Competence;
	}

	@Override
	long getObjectId() {
		return competenceAssessment.getCompetence().getId();
	}

	private String getNotificationLink(AssessmentType assessmentType) {
		Context context = contextJsonParserService.parseContext(event.getContext());
		long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		long competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
		long competenceAssessmentId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE_ASSESSMENT);

		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, competenceId, competenceAssessmentId, assessmentType, assessmentManager, idEncoder,
				session, PageSection.STUDENT);
	}

}
