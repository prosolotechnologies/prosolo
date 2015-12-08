package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.EvaluationSubmissionNotification;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.notifications.EvaluationUpdater;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.web.ApplicationBean;

public class EvaluationGivenProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(EvaluationGivenProcessor.class);
	
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	private EvaluationUpdater evaluationUpdater;
	
	public EvaluationGivenProcessor(Event event, Session session, 
					NotificationManager notificationManager, 
					NotificationsSettingsManager notificationsSettingsManager, 
					SessionMessageDistributer messageDistributer,
					ApplicationBean applicationBean, EvaluationUpdater evaluationUpdater) {
		super(event, session, notificationManager, notificationsSettingsManager);
		this.messageDistributer = messageDistributer;
		this.applicationBean = applicationBean;
		this.evaluationUpdater = evaluationUpdater;
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;	
		User receiver = evSubmission.getRequest().getMaker();
		users.add(receiver);
		
		return users;
	}

	@Override
	User getSender() {
		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;	
		return evSubmission.getMaker();
	}

	@Override
	String getNotificationMessage() {
		EvaluationSubmission evSubmission = (EvaluationSubmission) resource;
		return evSubmission.getMessage();
	}

	@Override
	void afterProcessing(Notification notification, Session session) {
		/*
		 * If user assessed is online, update the number of assessments for this resource.
		 * This number of assessments is being updated in session scoped beans used for displaying
		 * number of evaluations on Learn and Profile pages.
		 */
		long receiverId = notification.getReceiver().getId();
		EvaluationSubmissionNotification eSNotification = (EvaluationSubmissionNotification) notification;
		EvaluationSubmission evSubmission = eSNotification.getEvaluationSubmission();
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			messageDistributer.distributeMessage(
				ServiceType.UPDATE_EVALUAIION_DATA,
				receiverId, 
				evSubmission.getId(), 
				null, 
				null);
		} else {
			HttpSession userSession = applicationBean.getUserSession(receiverId);
			
			try {
				evaluationUpdater.updateEvaluationData(
					evSubmission.getId(),
					userSession,
					session);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}

	@Override
	boolean isAdditionalConditionMet(User sender, User receiver) {
		return true;
	}
	
}
