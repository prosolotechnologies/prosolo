package org.prosolo.services.notifications.eventprocessing;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.web.ApplicationBean;

public class JoinGoalRequestApprovedProcessor extends RequestWithoutCommentEventProcessor {

	private SessionMessageDistributer messageDistributer;
	private LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater;
	private ApplicationBean applicationBean;
	
	public JoinGoalRequestApprovedProcessor(Event event, Session session, 
					NotificationManager notificationManager, 
					NotificationsSettingsManager notificationsSettingsManager, 
					SessionMessageDistributer messageDistributer,
					LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater, 
					ApplicationBean applicationBean) {
		super(event, session, notificationManager, notificationsSettingsManager);
		this.messageDistributer = messageDistributer;
		this.learningGoalRecommendationCacheUpdater = learningGoalRecommendationCacheUpdater;
		this.applicationBean = applicationBean;
	}

	@Override
	void afterProcessing(Notification notification, Session session) {
		Request request = (Request) resource;
		checkRecommendedationsForAcceptedLearningGoal((TargetLearningGoal) request.getResource(), 
				request.getMaker(), 
				session);
	}
	
	private void checkRecommendedationsForAcceptedLearningGoal(TargetLearningGoal targetGoal, User receiver, Session session) {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			messageDistributer.distributeMessage(
					ServiceType.JOINED_LEARNING_GOAL,
					receiver.getId(), 
					targetGoal.getLearningGoal().getId(), 
					null, 
					null);
		} else {
			HttpSession userSession = applicationBean.getUserSession(receiver.getId());

			learningGoalRecommendationCacheUpdater.removeLearningGoalRecommendation(
					receiver.getId(), 
					targetGoal.getLearningGoal().getId(), 
					userSession, 
					session);
		}
	}
}
