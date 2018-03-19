package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public class CompetenceAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

	private static Logger logger = Logger.getLogger(CompetenceAssessmentCommentEventProcessor.class);

	private ContextJsonParserService ctxJsonParser;
	private long credentialId;
	private long credentialAssessmentId;
	private CompetenceAssessment compAssessment;

	public CompetenceAssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													 AssessmentManager assessmentManager, ContextJsonParserService ctxJsonParser) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
		this.ctxJsonParser = ctxJsonParser;
		Context context = this.ctxJsonParser.parseContext(event.getContext());
		credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		compAssessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getTarget().getId());
		if (credentialId > 0) {
			credentialAssessmentId = AssessmentLinkUtil.getCredentialAssessmentId(
					context, credentialId, compAssessment.getId(), assessmentManager, session);
		}
	}

	@Override
	protected List<Long> getParticipantIds(long assessmentId) {
		return assessmentManager.getCompetenceDiscussionParticipantIds(assessmentId);
	}

	@Override
	protected AssessmentBasicData getBasicAssessmentInfo(long assessmentId) {
		return assessmentManager.getBasicAssessmentInfoForCompetenceAssessment(assessmentId);
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
		return credentialAssessmentId > 0 ? credentialId : compAssessment.getCompetence().getId();
	}

	@Override
	protected String getNotificationLink(PageSection section, AssessmentType assessmentType) {
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				credentialId,
				credentialAssessmentId,
				compAssessment.getCompetence().getId(),
				compAssessment.getId(),
				assessmentType,
				idEncoder,
				section);
	}

}
