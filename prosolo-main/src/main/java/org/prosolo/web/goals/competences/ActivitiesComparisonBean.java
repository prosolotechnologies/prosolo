package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="activitiesComparisonBean")
@Component("activitiesComparisonBean")
@Scope("view")
public class ActivitiesComparisonBean implements Serializable {
	
	private static final long serialVersionUID = -5852232246311505052L;

	@Autowired private CompetenceManager compManager;

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private UserData user;
	private List<ActivityWallData> activities = new ArrayList<ActivityWallData>();
	private boolean hasCompetence = false;
	
	/*
	 * OPERATIONS
	 */
	public void initActivities(final UserData user, long goalId, final CompetenceDataCache competenceDataCache) {
		this.user = user;
		activities.clear();
		
		TargetCompetence targetCompetence = compManager.getTargetCompetence(
				user.getId(), 
				competenceDataCache.getData().getCompetenceId(), 
				goalId);
		
		if (targetCompetence == null) {
			hasCompetence = false;
		} else {
			hasCompetence = true;
			List<TargetActivity> activitiesForComparison = targetCompetence.getTargetActivities();
			
			if (activitiesForComparison != null && !activitiesForComparison.isEmpty()) {
				for (TargetActivity targetActivity : activitiesForComparison) {
					ActivityWallData actData = compWallActivityConverter.convertTargetActivityToActivityWallData(
							null, 
							targetActivity, 
							loggedUser.getUser(), 
							loggedUser.getLocale(), 
							true, 
							false);
					
					if (competenceDataCache.getActivities() != null && !competenceDataCache.getActivities().isEmpty()) {
						for (ActivityWallData loggedUserActData : competenceDataCache.getActivities()) {
							if (loggedUserActData.getActivity().getId() == targetActivity.getActivity().getId()) {
								actData.setCanBeAdded(false);
								break;
							}
						}
					}
					
					activities.add(actData);
				}
			}
		}
		
    	long targetComp = competenceDataCache.getData().getId();

    	Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", "learn.targetGoal."+competenceDataCache.getParentGoalDataCache().getData().getTargetGoalId()+".targetComp"+targetComp);
    	parameters.put("targetComp", String.valueOf(targetComp));
    	parameters.put("user", String.valueOf(user.getId()));
    	
    	actionLogger.logEvent(
    			EventType.ACTIVITIES_COMPARE, 
    			TargetCompetence.class.getSimpleName(), 
    			targetComp, 
    			parameters);
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public UserData getUser() {
		return user;
	}

	public List<ActivityWallData> getActivities() {
		return activities;
	}

	public boolean isHasCompetence() {
		return hasCompetence;
	}
	
}
