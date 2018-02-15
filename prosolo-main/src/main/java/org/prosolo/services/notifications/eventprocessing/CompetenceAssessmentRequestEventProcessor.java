package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.common.event.context.LearningContextUtil;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CompetenceAssessmentRequestEventProcessor extends NotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompetenceAssessmentRequestEventProcessor.class);

	private ContextJsonParserService ctxJsonParserService;
	private AssessmentManager assessmentManager;
	private CompetenceAssessment assessment;

	public CompetenceAssessmentRequestEventProcessor(Event event, Session session, NotificationManager notificationManager,
													 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
													 ContextJsonParserService ctxJsonParserService, AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.ctxJsonParserService = ctxJsonParserService;
		this.assessmentManager = assessmentManager;
		assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
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
		return ResourceType.Competence;
	}

	@Override
	long getObjectId() {
		return assessment.getCompetence().getId();
	}

	private String getNotificationLink(PageSection section) {
		Context ctx = ctxJsonParserService.parseContext(event.getContext());
		long credId = LearningContextUtil.getIdFromContext(ctx, ContextName.CREDENTIAL);

		if (credId > 0) {
			long credAssessmentId = assessmentManager
					.getCredentialAssessmentIdForCompetenceAssessment(credId, assessment.getId(), session);
			if (credAssessmentId > 0) {
				return section.getPrefix() +
						"/credentials/" +
						idEncoder.encodeId(credId) +
						"/assessments/" +
						idEncoder.encodeId(credAssessmentId);
			}
		}

		//if student section (PEER assessment) and cred id is not passed or credential assessment does not exist, we create notification for competence assessment page
		if (section == PageSection.STUDENT) {
			return section.getPrefix() + "/competences/" +
					idEncoder.encodeId(assessment.getCompetence().getId()) +
					"/assessments/" +
					idEncoder.encodeId(assessment.getId());
		}
		return null;
	}

}
