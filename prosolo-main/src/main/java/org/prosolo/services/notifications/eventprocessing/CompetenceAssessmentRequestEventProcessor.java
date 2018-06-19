package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
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

public class CompetenceAssessmentRequestEventProcessor extends AssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentRequestEventProcessor.class);

	private ContextJsonParserService ctxJsonParserService;
	private AssessmentManager assessmentManager;
	private CredentialManager credentialManager;
	private Competence1Manager competenceManager;
	private CompetenceAssessment assessment;
	private long credentialId;
	private Context context;

	public CompetenceAssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													 ContextJsonParserService ctxJsonParserService, AssessmentManager assessmentManager,
													 CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.ctxJsonParserService = ctxJsonParserService;
		this.assessmentManager = assessmentManager;
		this.credentialManager = credentialManager;
		this.competenceManager = competenceManager;
		assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
		context = ctxJsonParserService.parseContext(event.getContext());
		credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
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
	protected long getAssessorId() {
		return assessment.getAssessor() != null
			? assessment.getAssessor().getId()
			: 0;
	}

	@Override
	protected long getStudentId() {
		return assessment.getStudent().getId();
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		/*
		if credential id is available blind assessment mode for credential is retrieved because
		notification is generated for credential assessment page, otherwise competence blind assessment mode
		is retrieved
		 */
		return credentialId > 0
				? credentialManager.getCredentialBlindAssessmentModeForAssessmentType(credentialId, assessment.getType())
				: competenceManager.getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(assessment.getCompetence().getId(), assessment.getType());
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
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, assessment.getCompetence().getId(), assessment.getId(), assessment.getType(), assessmentManager, idEncoder, session, section);
	}

}
