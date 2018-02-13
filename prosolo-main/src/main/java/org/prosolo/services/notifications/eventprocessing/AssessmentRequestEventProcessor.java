package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class AssessmentRequestEventProcessor extends NotificationEventProcessor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AssessmentRequestEventProcessor.class);

	private CredentialAssessment assessment;
	
	public AssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
										   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
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
	long getSenderId() {
		return event.getActorId();
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
		return section.getPrefix() + "/credentials/" +
				idEncoder.encodeId(assessment.getTargetCredential().getCredential().getId()) +
				"/assessments/" +
				idEncoder.encodeId(assessment.getId());
	}

}
