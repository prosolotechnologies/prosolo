package org.prosolo.services.interfaceSettings.eventProcessors;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater;
import org.prosolo.services.event.Event;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.web.ApplicationBean;

public class JoinGoalApprovedInterfaceEventProcessor extends InterfaceEventProcessor {

	private LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater;
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	
	
	public JoinGoalApprovedInterfaceEventProcessor(Session session, Event event, BaseEntity object,
			LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater, 
			SessionMessageDistributer messageDistributer,
			ApplicationBean applicationBean) {
		super(session, event, object);
		this.learningGoalRecommendationCacheUpdater = learningGoalRecommendationCacheUpdater;
		this.messageDistributer = messageDistributer;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		Request request = (Request) session.merge(object);
		
		checkRecommendedationsForAcceptedLearningGoal(
				(TargetLearningGoal) request.getResource(), 
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
