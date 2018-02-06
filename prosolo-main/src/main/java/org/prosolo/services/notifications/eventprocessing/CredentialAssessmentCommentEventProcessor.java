package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.List;

public class CredentialAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

	private static Logger logger = Logger.getLogger(CredentialAssessmentCommentEventProcessor.class);

	public CredentialAssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                     NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                     AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
	}

	@Override
	protected List<Long> getParticipantIds(long assessmentId) {
		return assessmentManager.getCredentialDiscussionParticipantIds(assessmentId);
	}

	@Override
	protected AssessmentBasicData getBasicAssessmentInfo(long assessmentId) {
		return assessmentManager.getBasicAssessmentInfoForCredentialAssessment(assessmentId);
	}

	@Override
	protected long getCredentialId(Event event) {
		CredentialAssessment ca = getCredentialAssessment(event);
		return ca.getTargetCredential().getCredential().getId();
	}

	private CredentialAssessment getCredentialAssessment(Event event) {
		return (CredentialAssessment) session.load(CredentialAssessment.class, event.getTarget().getId());
	}

	@Override
	protected long getCredentialAssessmentId(Event event) {
		return getCredentialAssessment(event).getId();
	}

}
