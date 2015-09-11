/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.LearnActivityCacheUpdater;
import org.prosolo.web.goals.LearningGoalsBean;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.LearnActivityCacheUpdater")
public class LearnActivityCacheUpdaterImpl extends AbstractManagerImpl implements LearnActivityCacheUpdater, Serializable {
	
	private static final long serialVersionUID = 1203711043948995349L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LearnActivityCacheUpdaterImpl.class);
	
	@Override
	public boolean updateActivityOutcome(long targetActivityId, Outcome outcome, HttpSession userSession, Session session) {
		if (userSession != null) {
			LearningGoalsBean userLearningGoalBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
			
			if (userLearningGoalBean != null) {
				return userLearningGoalBean.getData().updateActivityOutcome(targetActivityId, outcome);
			}
		}
		return false;
	}
}
