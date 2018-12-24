package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class CompetenceAssessmentNotificationEventProcessor extends AssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentNotificationEventProcessor.class);

	private CredentialManager credentialManager;
	private Competence1Manager competenceManager;
	private CompetenceAssessment assessment;
	private long credentialId;
	private Context context;

	public CompetenceAssessmentNotificationEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                          NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                          ContextJsonParserService ctxJsonParserService, CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.credentialManager = credentialManager;
		this.competenceManager = competenceManager;
		assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
		context = ctxJsonParserService.parseContext(event.getContext());
		credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
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

	public CompetenceAssessment getAssessment() {
		return assessment;
	}

	public Context getContext() {
		return context;
	}

	public long getCredentialId() {
		return credentialId;
	}
}
