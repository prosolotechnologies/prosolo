package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CompetenceAssessmentApprovedEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CompetenceAssessmentApprovedEventProcessor.class);

	private AssessmentManager assessmentManager;

	public CompetenceAssessmentApprovedEventProcessor(Event event, Session session, NotificationManager notificationManager,
													  NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													  AssessmentManager assessmentManager) {
		super(event, event.getObject().getId(), session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
		this.assessmentManager = assessmentManager;
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
			receivers.add(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(), false, PageSection.STUDENT));
			return receivers;
		} catch (Exception e) {
			logger.error("Error", e);
			return new ArrayList<>();
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Approved;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Competence;
	}

	@Override
	long getObjectId() {
		return assessment.getCompetence().getId();
	}

	private String getNotificationLink() {
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, assessment.getCompetence().getId(), assessment.getId(), assessment.getType(), assessmentManager, idEncoder, session, PageSection.STUDENT);
	}

}
