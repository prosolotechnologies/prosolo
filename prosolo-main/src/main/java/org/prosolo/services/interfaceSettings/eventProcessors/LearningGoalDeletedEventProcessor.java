package org.prosolo.services.interfaceSettings.eventProcessors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.LearnPageCacheUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.portfolio.PortfolioBean;

public class LearningGoalDeletedEventProcessor extends InterfaceEventProcessor {
	
	private SessionMessageDistributer messageDistributer;
	private ApplicationBean applicationBean;
	private ActivityManager activityManager;
	private LearningGoalManager goalManager;
	private LearnPageCacheUpdater learnPageCacheUpdater;
	
	public LearningGoalDeletedEventProcessor(Session session, Event event, BaseEntity object,
			SessionMessageDistributer messageDistributer, ActivityManager activityManager, 
			LearningGoalManager goalManager, LearnPageCacheUpdater learnPageCacheUpdater,
			ApplicationBean applicationBean) {
		super(session, event, object);
		this.messageDistributer = messageDistributer;
		this.activityManager = activityManager;
		this.goalManager = goalManager;
		this.learnPageCacheUpdater = learnPageCacheUpdater;
		this.applicationBean = applicationBean;
	}

	@Override
	void process() {
		updateAfterGoalDeleted((TargetLearningGoal) object, event.getActorId(), session);
	}
	
	private void updateAfterGoalDeleted(TargetLearningGoal goal, long actorId, Session session) {
		// update Portfolio cache of online user if exists
    	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(actorId).getAttribute("portfolio");
    	
    	if (portfolioBean != null)
    		portfolioBean.populateWithActiveCompletedCompetences();
    	
    	
    	// update collaborator list of goal's members
    	goal = activityManager.merge(goal, session);
    	
    	//for Nikola: goal id or target learning goal id
    	List<User> collaborators = goalManager.retrieveCollaborators(goal.getId(), actorId, session);
    	
    	Iterator<User> iterator = collaborators.iterator();
		
		while (iterator.hasNext()) {
			User user = (User) iterator.next();
			
			if (user.getId() == actorId) {
				iterator.remove();
				break;
			}
		}
    	
    	for (User user : collaborators) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("learningGoal", String.valueOf(goal.getId()));
				
				messageDistributer.distributeMessage(
						ServiceType.REMOVE_GOAL_COLLABORATOR,
						user.getId(), 
						actorId, 
						null, 
						parameters);
			} else {
				HttpSession userSession = applicationBean.getUserSession(user.getId());
				
				//for Nikola: goal.getLearningGoal() ?
				learnPageCacheUpdater.removeCollaboratorFormGoal(actorId, goal.getLearningGoal(), userSession);
			}
		}
	}
	

}
