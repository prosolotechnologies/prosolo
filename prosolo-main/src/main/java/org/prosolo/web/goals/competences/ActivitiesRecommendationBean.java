package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.recommendation.ActivityRecommender;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="activitiesRecommendation")
@Component("activitiesRecommendation")
@Scope("view")
public class ActivitiesRecommendationBean implements Serializable {
	
	private static final long serialVersionUID = -7603602645581875929L;
	
	private Logger logger = Logger.getLogger(ActivitiesRecommendationBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private ActivityRecommender activityRecommender;
	
	private CompetenceDataCache compData;
	private final int limit = 5;

	public void initializeActivities() {
		System.out.println("initialize Activities called");
		if (compData == null) {
			logger.error("compData should be set at this point");
			return;
		}
		
		if (compData.getRecommendedActivities() != null) {
			return;
		}
		
		List<Activity> recommendedActivities = activityRecommender.getRecommendedActivitiesForCompetence(
				compData.getData().getCompetenceId(),
				compData.getActivities(),
				limit);
		
		if (recommendedActivities != null && !recommendedActivities.isEmpty()) {
			List<ActivityWallData> convertedActivities = compWallActivityConverter.convertActivities(
					recommendedActivities,
					loggedUser.getUser(),
					loggedUser.getLocale(),
					true, 
					false);
			compData.setRecommendedActivities(convertedActivities);
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public void setCompData(CompetenceDataCache compData) {
		this.compData = compData;
	}
	
}
