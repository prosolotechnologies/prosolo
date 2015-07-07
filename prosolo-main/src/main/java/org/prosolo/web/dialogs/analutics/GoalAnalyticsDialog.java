package org.prosolo.web.dialogs.analutics;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.stats.LearningGoalStatistics;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.dialogs.analutics.data.GoalAnalyticsData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "goalAnalyticsBean")
@Component("goalAnalyticsBean")
@Scope("view")
public class GoalAnalyticsDialog implements Serializable {

	private static final long serialVersionUID = 4471180999274817372L;

	private static Logger logger = Logger.getLogger(GoalAnalyticsDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;

	@Autowired private LearningGoalStatistics goalStatistics;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired private DefaultManager defaultManager;
	
	private LearningGoal goal;
	private GoalAnalyticsData data = new GoalAnalyticsData();;
	
	/*
	 * ACTIONS
	 */

	public void initializeForTargetLearningGoal(long targetGoalId, String context) {
		try {
			TargetLearningGoal targetGoal = defaultManager.loadResource(TargetLearningGoal.class, targetGoalId);
			this.goal = targetGoal.getLearningGoal();
			
			data = new GoalAnalyticsData();
			data.setCompletionTime(goalStatistics.getGoalCompletionTime(goal));
			
			actionLogger.logServiceUse(
					ComponentName.GOAL_ANALYTICS_DIALOG, 
					"context", context,
					"targetGoalId", String.valueOf(targetGoalId));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public LearningGoal getGoal() {
		return goal;
	}

	public GoalAnalyticsData getData() {
		return data;
	}

}
