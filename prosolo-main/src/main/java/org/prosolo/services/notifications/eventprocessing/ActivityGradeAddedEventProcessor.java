package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.GradingMode;
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
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class ActivityGradeAddedEventProcessor extends GradeAddedEventProcessor {

	private static Logger logger = Logger.getLogger(ActivityGradeAddedEventProcessor.class);

	private ContextJsonParserService contextJsonParserService;
	private AssessmentManager assessmentManager;

	private CredentialManager credentialManager;
	private Competence1Manager competenceManager;

	private ActivityAssessment assessment;
	private long credentialId;
	private long competenceId;
	private Context context;

	public ActivityGradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
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
		competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
		assessment = (ActivityAssessment) session.load(ActivityAssessment.class, event.getObject().getId());
	}

	@Override
	protected boolean shouldNotificationBeGenerated() {
		//notification should not be generated for self assessment and auto graded assessment
		return assessment.getType() != AssessmentType.SELF_ASSESSMENT && assessment.getActivity().getGradingMode() != GradingMode.AUTOMATIC;
	}

	@Override
	protected long getStudentId() {
		return assessment.getAssessment().getStudent().getId();
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		return credentialId > 0
				? credentialManager.getCredentialBlindAssessmentModeForAssessmentType(credentialId, assessment.getType())
				: competenceManager.getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(competenceId, assessment.getType());
	}

	@Override
	protected long getAssessorId() {
		return assessment.getAssessment().getAssessor() != null
				? assessment.getAssessment().getAssessor().getId()
				: 0;
	}

	@Override
	protected String getNotificationLink() {
		long competenceAssessmentId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE_ASSESSMENT);
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, competenceId, competenceAssessmentId, assessment.getType(), assessmentManager, idEncoder,
				session, PageSection.STUDENT);
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.ActivityAssessment;
	}

}
