package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class GradeAddedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(GradeAddedEventProcessor.class);

	private ContextJsonParserService contextJsonParserService;

	public GradeAddedEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder, ContextJsonParserService contextJsonParserService) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.contextJsonParserService = contextJsonParserService;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();

		boolean isNotSelfAssessment = false;
		User student = null;

		if (event.getObject() instanceof CredentialAssessment) {
			CredentialAssessment assessment = (CredentialAssessment) session.load(CredentialAssessment.class, event.getObject().getId());
			isNotSelfAssessment = assessment.getType() != AssessmentType.SELF_ASSESSMENT;
			student = assessment.getStudent();
		} else if (event.getObject() instanceof CompetenceAssessment) {
			CompetenceAssessment assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());
			isNotSelfAssessment = assessment.getType() != AssessmentType.SELF_ASSESSMENT;
			student = assessment.getStudent();
		} else if (event.getObject() instanceof ActivityAssessment) {
			ActivityAssessment assessment = (ActivityAssessment) session.load(ActivityAssessment.class, event.getObject().getId());
			isNotSelfAssessment = assessment.getType() != AssessmentType.SELF_ASSESSMENT;
			student = assessment.getAssessment().getStudent();
		}

		try {
			// this notification should be created only if it is not self-assessment
			if (isNotSelfAssessment) {
				receivers.add(new NotificationReceiverData(student.getId(), getNotificationLink(), false, PageSection.STUDENT));
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
		} else {
			logger.error("GradeAddedEventProcessor can not process assessment of type " + event.getObject().getClass());
			return null;
		}
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}

	private String getNotificationLink() {
		Context context = contextJsonParserService.parseContext(event.getContext());
		long credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);

		// if there is credential id, regardless the type of assessment, we should send user to the credential assessment page
		if (credentialId > 0) {
			long credentialAssessmentId = 0;

			if (event.getObject() instanceof CompetenceAssessment) {
				CompetenceAssessment assessment = (CompetenceAssessment) session.load(CompetenceAssessment.class, event.getObject().getId());

				// TODO: fix this approach of returning the first credential assessment id
				credentialAssessmentId = assessment.getCredentialAssessments().iterator().next().getId();
			} else {
				// it is CredentialAssessment
				credentialAssessmentId = event.getObject().getId();
			}
			return "/credentials/" +
					idEncoder.encodeId(credentialId) +
					"/assessments/" +
					idEncoder.encodeId(credentialAssessmentId);
		}

		// if there is no credentialId, then the assessment was probably made from a dedicated page for competency assessments
		if (event.getObject() instanceof CompetenceAssessment) {
			long competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);

			if (competenceId > 0) {
				return "/competences/" +
						idEncoder.encodeId(competenceId) +
						"/assessments/" +
						idEncoder.encodeId(event.getObject().getId());
			} else {
				logger.error("Can not extract credential id from the context: " + event.getContext());
			}
		}
		return null;
	}

}
