package org.prosolo.recommendation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.recommendation.ActivityRecommender;
import org.prosolo.services.es.ESAssociationRulesSearch;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.rest.clients.RecommendationServicesRest;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("org.prosolo.recommendation.ActivityRecommender")
public class ActivityRecommenderImpl implements ActivityRecommender, Serializable {
	
	private static final long serialVersionUID = 4432418449386195546L;
	
	private Logger logger = Logger.getLogger(ActivityRecommenderImpl.class);
	
	@Autowired private RecommendationServicesRest recommendationServices;
	@Autowired private ActivityManager activityManager;
	@Autowired private ESAssociationRulesSearch esAssociationRulesSearch;
	
	@Override
	public List<Activity> getRecommendedActivitiesForCompetence(long competenceId, List<ActivityWallData> ignoreActivities, int limit) {
		System.out.println("getRecommendedActivitiesForCompetence:"+competenceId);
		
		List<Activity> activities = new ArrayList<Activity>();
	 
		try {
			List<ActivityAccessCount> frequentActivities = null;
			
			if (ignoreActivities.isEmpty()) {
				frequentActivities = esAssociationRulesSearch.findFrequentCompetenceActivities(competenceId, limit);
			} else {
				List<Long> alreadyAddedActivities = new ArrayList<Long>();
				
				for (ActivityWallData awData : ignoreActivities) {
					alreadyAddedActivities.add(awData.getId());
				}
				
				frequentActivities = esAssociationRulesSearch.findMatchingActivitiesForCompetenceInAssociationRules(alreadyAddedActivities,
						competenceId, limit);
			}
			
			for (ActivityAccessCount count : frequentActivities) {
				try {
					Activity activity=activityManager.loadResource(Activity.class,count.getActivityId());
					activities.add(activity);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}
		return activities;
	}

	@Override
	public List<Activity> getRelatedActivities(long competenceId, long activityId, int limit) {
		List<Activity> relatedActivities = new ArrayList<Activity>();
		
		try {
			List<Long> alreadyAddedActivities = new ArrayList<Long>();
			List<ActivityAccessCount> relatedActivitiesCounters;
			
			relatedActivitiesCounters = esAssociationRulesSearch.findRelatedActivitiesForCompetenceAndActivityInAssociationRules(
					alreadyAddedActivities, competenceId, activityId, limit);
			
			for (ActivityAccessCount count : relatedActivitiesCounters) {
				try {
					Activity activity = activityManager.loadResource(Activity.class, count.getActivityId());
					relatedActivities.add(activity);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}
		return relatedActivities;
	}
}
