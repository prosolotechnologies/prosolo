package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class GradeAddedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(GradeAddedEventProcessor.class);

	private ContextJsonParserService contextJsonParserService;
	private AssessmentManager assessmentManager;

	public GradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
									ContextJsonParserService contextJsonParserService, AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.contextJsonParserService = contextJsonParserService;
		this.assessmentManager = assessmentManager;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return sender != receiver;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();

		boolean shouldGenerateNotification = false;
		User student = null;
		AssessmentType aType = null;
		LearningResourceType resType = null;

		if (event.getObject() instanceof CredentialAssessment) {
			CredentialAssessment assessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
			shouldGenerateNotification = assessment.getType() != AssessmentType.SELF_ASSESSMENT;
			student = assessment.getStudent();
			aType = assessment.getType();
			resType = LearningResourceType.CREDENTIAL;
		} else if (event.getObject() instanceof CompetenceAssessment) {
			CompetenceAssessment assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
			shouldGenerateNotification = assessment.getType() != AssessmentType.SELF_ASSESSMENT;
			student = assessment.getStudent();
			aType = assessment.getType();
			resType = LearningResourceType.COMPETENCE;
		} else if (event.getObject() instanceof ActivityAssessment) {
			ActivityAssessment assessment = (ActivityAssessment) session.load(ActivityAssessment.class, event.getObject().getId());
			//we don't generate notification for activity with automatic grading
			shouldGenerateNotification = assessment.getType() != AssessmentType.SELF_ASSESSMENT && assessment.getActivity().getGradingMode() != GradingMode.AUTOMATIC;
			student = assessment.getAssessment().getStudent();
			aType = assessment.getType();
			resType = LearningResourceType.ACTIVITY;
		}

		try {
			// this notification should be created only if it is not self-assessment
			if (shouldGenerateNotification) {
				receivers.add(new NotificationReceiverData(student.getId(), getNotificationLink(aType, resType), false, PageSection.STUDENT));
			}
			return receivers;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.GradeAdded;
	}

	@Override
	ResourceType getObjectType() {
		if (event.getObject() instanceof CredentialAssessment) {
			return ResourceType.CredentialAssessment;
		} else if (event.getObject() instanceof CompetenceAssessment) {
			return ResourceType.CompetenceAssessment;
		} else if (event.getObject() instanceof ActivityAssessment) {
			return ResourceType.ActivityAssessment;
		} else {
			logger.error("GradeAddedEventProcessor can not process assessment of type " + event.getObject().getClass());
			return null;
		}
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}

	@Override
	public ResourceType getTargetType() {
		if (event.getObject() instanceof CredentialAssessment) {
			return ResourceType.Credential;
		} else if (event.getObject() instanceof CompetenceAssessment) {
			return ResourceType.Competence;
		} else if (event.getObject() instanceof ActivityAssessment) {
			return ResourceType.Activity;
		} else {
			logger.error("GradeAddedEventProcessor can not process assessment of type " + event.getObject().getClass());
			return null;
		}
	}

	@Override
	public long getTargetId() {
		if (event.getObject() instanceof CredentialAssessment) {
			CredentialAssessment assessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
			return assessment.getTargetCredential().getCredential().getId();
		} else if (event.getObject() instanceof CompetenceAssessment) {
			CompetenceAssessment assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
			return assessment.getCompetence().getId();
		} else if (event.getObject() instanceof ActivityAssessment) {
			ActivityAssessment assessment = (ActivityAssessment) session.load(ActivityAssessment.class, event.getObject().getId());
			return assessment.getActivity().getId();
		} else {
			logger.error("GradeAddedEventProcessor can not process assessment of type " + event.getObject().getClass());
			return 0;
		}
	}

	private String getNotificationLink(AssessmentType assessmentType, LearningResourceType resType) {
		Context context = contextJsonParserService.parseContext(event.getContext());
		long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
		long competenceId = 0;
		long competenceAssessmentId = 0;
		if (resType != LearningResourceType.CREDENTIAL) {
			competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
			competenceAssessmentId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE_ASSESSMENT);
		}
		return AssessmentLinkUtil.getAssessmentNotificationLink(
				context, credentialId, competenceId, competenceAssessmentId, assessmentType, assessmentManager, idEncoder,
				session, PageSection.STUDENT);
	}

}
