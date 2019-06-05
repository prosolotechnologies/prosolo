package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public class ActivityAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

	private static Logger logger = Logger.getLogger(ActivityAssessmentCommentEventProcessor.class);

	private long credentialId;
	private long credentialAssessmentId;
	private long competenceId;
	private long compAssessmentId;

	private AssessmentBasicData assessmentBasicInfo;

	public ActivityAssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
												   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
												   AssessmentManager assessmentManager, CredentialManager credentialManager, Competence1Manager competenceManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager, credentialManager, competenceManager);
		Context context = ContextJsonParserService.parseContext(event.getContext());
		competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
		compAssessmentId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE_ASSESSMENT);
		credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		credentialAssessmentId = AssessmentLinkUtil.getCredentialAssessmentId(
				context, compAssessmentId, assessmentManager, session);
        assessmentBasicInfo = assessmentManager.getBasicAssessmentInfoForActivityAssessment(event.getTarget().getId());
    }

	@Override
	protected List<Long> getParticipantIds(long assessmentId) {
		return assessmentManager.getActivityDiscussionParticipantIds(assessmentId);
	}

	@Override
	protected AssessmentBasicData getBasicAssessmentInfo() {
		return assessmentBasicInfo;
	}

	@Override
	protected BlindAssessmentMode getBlindAssessmentMode() {
		return ((CompetenceAssessment) session.load(CompetenceAssessment.class, compAssessmentId)).getBlindAssessmentMode();
	}

    @Override
    protected long getAssessorId() {
        return getBasicAssessmentInfo().getAssessorId();
    }

    @Override
    protected long getStudentId() {
        return getBasicAssessmentInfo().getStudentId();
    }

    @Override
	protected ResourceType getObjectType() {
		/*
		if credential assessment id is available we generate notification for credential and credential resource type
		is returned, otherwise competence type is returned
		 */
		return credentialAssessmentId > 0 ? ResourceType.Credential : ResourceType.Competence;
	}

	@Override
	protected long getObjectId() {
		return credentialAssessmentId > 0 ? credentialId : competenceId;
	}

	@Override
	protected String getNotificationLink(PageSection section, AssessmentType assessmentType) {
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				credentialId,
				credentialAssessmentId,
				competenceId,
				compAssessmentId,
				assessmentType,
				idEncoder,
				section);
	}


}
