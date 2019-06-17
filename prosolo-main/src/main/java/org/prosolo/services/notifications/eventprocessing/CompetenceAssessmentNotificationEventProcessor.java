package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class CompetenceAssessmentNotificationEventProcessor extends AssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentNotificationEventProcessor.class);

	protected CompetenceAssessment assessment;
	protected long credentialAssessmentId;
	protected long credentialId;
	protected Context context;

	public CompetenceAssessmentNotificationEventProcessor(Event event, long competenceAssessmentId, Session session, NotificationManager notificationManager,
														  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
														  AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, competenceAssessmentId);
		context = ContextJsonParserService.parseContext(event.getContext());
		credentialId = assessment.getTargetCredential().getCredential().getId();
		credentialAssessmentId = AssessmentLinkUtil.getCredentialAssessmentId(
				context, assessment.getId(), assessmentManager, session);
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
		return assessment.getBlindAssessmentMode();
	}

}
