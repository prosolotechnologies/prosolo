package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public class CredentialAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

	private static Logger logger = Logger.getLogger(CredentialAssessmentCommentEventProcessor.class);

	private CredentialAssessment credentialAssessment;
	private AssessmentBasicData assessmentBasicData;

	public CredentialAssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													 AssessmentManager assessmentManager, CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager, credentialManager, competenceManager);
		credentialAssessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getTarget().getId());
		assessmentBasicData = assessmentManager.getBasicAssessmentInfoForCredentialAssessment(event.getTarget().getId());
	}

	@Override
	protected List<Long> getParticipantIds(long assessmentId) {
		return assessmentManager.getCredentialDiscussionParticipantIds(assessmentId);
	}

	@Override
	protected AssessmentBasicData getBasicAssessmentInfo() {
		return assessmentBasicData;
	}

	@Override
	protected ResourceType getObjectType() {
		return ResourceType.Credential;
	}

	@Override
	protected long getObjectId() {
		return credentialAssessment.getTargetCredential().getCredential().getId();
	}

	@Override
	protected String getNotificationLink(PageSection section, AssessmentType assessmentType) {
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				credentialAssessment.getTargetCredential().getCredential().getId(),
				credentialAssessment.getId(),
				0,
				0,
				assessmentType,
				idEncoder,
				section);
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		return credentialAssessment.getBlindAssessmentMode();
	}

	@Override
	protected long getAssessorId() {
		return getBasicAssessmentInfo().getAssessorId();
	}

	@Override
	protected long getStudentId() {
		return getBasicAssessmentInfo().getStudentId();
	}

}
