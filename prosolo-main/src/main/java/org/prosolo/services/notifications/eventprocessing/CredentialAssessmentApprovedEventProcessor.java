package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CredentialAssessmentApprovedEventProcessor extends AssessmentNotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CredentialAssessmentApprovedEventProcessor.class);

	private CredentialManager credentialManager;

	private CredentialAssessment credentialAssessment;

	public CredentialAssessmentApprovedEventProcessor(Event event, Session session, NotificationManager notificationManager,
											NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, CredentialManager credentialManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.credentialManager = credentialManager;
		credentialAssessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
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
			receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(credentialAssessment.getType()), false, PageSection.STUDENT));
			return receivers;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	protected long getAssessorId() {
		return credentialAssessment.getAssessor() != null
				? credentialAssessment.getAssessor().getId()
				: 0;
	}

	@Override
	protected long getStudentId() {
		return credentialAssessment.getStudent().getId();
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		return credentialAssessment.getBlindAssessmentMode();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Approved;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Credential;
	}

	@Override
	long getObjectId() {
		return credentialAssessment.getTargetCredential().getCredential().getId();
	}

	private String getNotificationLink(AssessmentType aType) {
		long credAssessmentId = event.getObject().getId();
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				credentialAssessment.getTargetCredential().getCredential().getId(), credAssessmentId, 0, 0, aType, idEncoder, PageSection.STUDENT);
	}

}
