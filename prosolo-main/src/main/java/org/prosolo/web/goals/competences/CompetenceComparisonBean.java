package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="competenceComparisonBean")
@Component("competenceComparisonBean")
@Scope("view")
public class CompetenceComparisonBean implements Serializable {
	
	private static final long serialVersionUID = 5023345322432761107L;

	@Autowired private CompetenceManager compManager;

	@Autowired private LearnBean goalBean;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private UserData user;
	private List<CompetenceDataCache> competences = new ArrayList<CompetenceDataCache>();
	
	/*
	 * OPERATIONS
	 */
	public void initCompetences(final UserData user, final long goalId) {
		this.user = user;
		competences.clear();
		
		GoalDataCache goalData = goalBean.getData().getDataForGoal(goalId);
		
		List<TargetCompetence> tComps = compManager.getTargetCompetences(user.getId(), goalId);
		
		for (TargetCompetence tComp : tComps) {
			if (tComp != null) {
				CompetenceDataCache compDataCache = ServiceLocator.getInstance().getService(CompetenceDataCache.class);
				compDataCache.init(tComp);
				
				// iterating through logged in user's competences for this goal
				for (CompetenceDataCache compData : goalData.getCompetences()) {
					if (compData.getData().getCompetenceId() == compDataCache.getData().getCompetenceId()) {
						compDataCache.getData().setUserHasCompetence(true);
						break;
					}
				}
				
				competences.add(compDataCache);
			}
		}
		
    	Map<String, String> parameters = new HashMap<String, String>();
    	parameters.put("context", "learn.targetGoal."+goalId+".competences");
    	parameters.put("targetGoalId", String.valueOf(goalId));
    	parameters.put("user", String.valueOf(user.getId()));
    	
    	actionLogger.logEvent(
    			EventType.COMPETENCES_COMPARE, 
    			TargetLearningGoal.class.getSimpleName(), 
    			goalId, 
    			parameters);
		
	}
	
	public void markCompetenceAsAdded(long compId) {
		if (compId > 0) {
			for (CompetenceDataCache competenceDataCache : competences) {
				if (competenceDataCache.getData().getCompetenceId() == compId) {
					competenceDataCache.getData().setUserHasCompetence(true);
					break;
				}
			}
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public List<CompetenceDataCache> getCompetences() {
		return competences;
	}

	public UserData getUser() {
		return user;
	}
	
}
