package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class CompetenceGradeAddedEventProcessor extends GradeAddedEventProcessor {

	private static Logger logger = Logger.getLogger(CompetenceGradeAddedEventProcessor.class);

	private AssessmentManager assessmentManager;

	private CompetenceAssessment assessment;
	private long credentialId;
	private Context context;

	public CompetenceGradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
											  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
											  AssessmentManager assessmentManager,
											  CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.assessmentManager = assessmentManager;
		context = ContextJsonParserService.parseContext(event.getContext());
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
		return assessment.getBlindAssessmentMode();
	}

	@Override
	protected long getAssessorId() {
		return assessment.getAssessor() != null
				? assessment.getAssessor().getId()
				: 0;
	}

	@Override
	protected String getNotificationLink() {
		Context context = ContextJsonParserService.parseContext(event.getContext());
		ApplicationPage page = ApplicationPage.getPageForURI(event.getPage());

		return AssessmentLinkUtil.getNotificationLinkForCompetenceAssessment(context, page, assessment.getType(), idEncoder, PageSection.STUDENT);
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.CompetenceAssessment;
	}

}
