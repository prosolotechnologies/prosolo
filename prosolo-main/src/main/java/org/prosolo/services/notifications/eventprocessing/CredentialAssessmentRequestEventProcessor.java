package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CredentialAssessmentRequestEventProcessor extends AssessmentNotificationEventProcessor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CredentialAssessmentRequestEventProcessor.class);

	private CredentialManager credentialManager;
	private CredentialAssessment assessment;
	
	public CredentialAssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													 CredentialManager credentialManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.credentialManager = credentialManager;
		assessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
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
		return assessment.getBlindAssessmentMode();
	}


	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Requested;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Credential;
	}

	@Override
	long getObjectId() {
		return assessment.getTargetCredential().getCredential().getId();
	}

	private String getNotificationLink(PageSection section) {
		//assessment type is Instructor if manage section and Peer if student section because in Self assessment there is no assessment request.
		AssessmentType assessmentType = section == PageSection.MANAGE ? AssessmentType.INSTRUCTOR_ASSESSMENT : AssessmentType.PEER_ASSESSMENT;
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				assessment.getTargetCredential().getCredential().getId(),
				assessment.getId(),
				0,
				0,
				assessmentType,
				idEncoder,
				section);
	}

}
