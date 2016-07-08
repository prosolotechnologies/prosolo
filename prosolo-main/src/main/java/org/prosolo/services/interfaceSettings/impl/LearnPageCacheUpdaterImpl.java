/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interfaceSettings.LearnPageCacheUpdater;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.LearnPageCacheUpdater")
public class LearnPageCacheUpdaterImpl implements LearnPageCacheUpdater, Serializable {
	
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LearnPageCacheUpdaterImpl.class);
	
	@Override
	public void removeCollaboratorFormGoal(long collaboratorToRemoveId, LearningGoal goal, HttpSession userSession) {
		if (userSession != null) {
			LearnBean userLearningGoalBean = (LearnBean) userSession.getAttribute("learninggoals");
			
			if (userLearningGoalBean != null) {
				GoalDataCache goalData = userLearningGoalBean.getData().getDataForGoal(goal);
				
				if (goalData != null) {
					Iterator<UserData> collaboratorIterator = goalData.getCollaborators().iterator();
	        		
	        		collaboratorLoop: while (collaboratorIterator.hasNext()) {
	        			UserData coll = (UserData) collaboratorIterator.next();
	        			
	        			if (coll.getId() == collaboratorToRemoveId) {
	        				collaboratorIterator.remove();
	        				break collaboratorLoop;
	        			}
	        		}
				}
			}
		}
	}
	
}
