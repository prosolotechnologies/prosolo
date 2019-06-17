package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class CredentialGradeAddedEventProcessor extends GradeAddedEventProcessor {

	private static Logger logger = Logger.getLogger(CredentialGradeAddedEventProcessor.class);

	private CredentialAssessment assessment;

	public CredentialGradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
											  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		assessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
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
		long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		long credentialAssessment = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL_ASSESSMENT);

		return AssessmentLinkUtil.getCredentialAssessmentPageLink(
				credentialId,
				credentialAssessment,
				assessment.getType(),
				idEncoder,
				PageSection.STUDENT);
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.CredentialAssessment;
	}

}
