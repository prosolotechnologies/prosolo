package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.List;

public class ActivityAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

	private static Logger logger = Logger.getLogger(ActivityAssessmentCommentEventProcessor.class);

	public ActivityAssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                   AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
	}

	@Override
	protected List<Long> getParticipantIds(long assessmentId) {
		return assessmentManager.getActivityDiscussionParticipantIds(assessmentId);
	}

	@Override
	protected AssessmentBasicData getBasicAssessmentInfo(long assessmentId) {
		return assessmentManager.getBasicAssessmentInfoForActivityAssessment(assessmentId);
	}

	@Override
	protected long getCredentialId(Event event) {
		return Long.parseLong(event.getParameters().get("credentialId"));
	}

	@Override
	protected long getCredentialAssessmentId(Event event) {
		return Long.parseLong(event.getParameters().get("credentialAssessmentId"));
	}

}
