package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.user.User;
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
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CompetenceGradeAddedEventProcessor extends GradeAddedEventProcessor {

	private static Logger logger = Logger.getLogger(CompetenceGradeAddedEventProcessor.class);

	private ContextJsonParserService contextJsonParserService;
	private AssessmentManager assessmentManager;
	private CredentialManager credentialManager;
	private Competence1Manager competenceManager;

	private CompetenceAssessment assessment;
	private long credentialId;
	private Context context;

	public CompetenceGradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
											  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
											  ContextJsonParserService contextJsonParserService, AssessmentManager assessmentManager,
											  CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.contextJsonParserService = contextJsonParserService;
		this.assessmentManager = assessmentManager;
		this.credentialManager = credentialManager;
		this.competenceManager = competenceManager;
		context = contextJsonParserService.parseContext(event.getContext());
		credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
	}

	@Override
	protected boolean shouldNotificationBeGenerated() {
		return assessment.getType() != AssessmentType.SELF_ASSESSMENT;
	}

	@Override
	protected long getStudentId() {
		return assessment.getStudent().getId();
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		return credentialId > 0
			? credentialManager.getCredentialBlindAssessmentModeForAssessmentType(credentialId, assessment.getType())
			: competenceManager.getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(assessment.getCompetence().getId(), assessment.getType());
	}

	@Override
	protected long getAssessorId() {
		return assessment.getAssessor() != null
				? assessment.getAssessor().getId()
				: 0;
	}

	@Override
	protected String getNotificationLink() {
		Context context = contextJsonParserService.parseContext(event.getContext());
		long competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
		long competenceAssessmentId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE_ASSESSMENT);
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, competenceId, competenceAssessmentId, assessment.getType(), assessmentManager, idEncoder,
				session, PageSection.STUDENT);
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.CompetenceAssessment;
	}

}