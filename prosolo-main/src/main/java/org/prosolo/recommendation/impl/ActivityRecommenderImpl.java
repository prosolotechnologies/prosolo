package org.prosolo.recommendation.impl;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
//@Service("org.prosolo.recommendation.ActivityRecommender")
public class ActivityRecommenderImpl 
	//implements ActivityRecommender, Serializable 
	{
	
//	private static final long serialVersionUID = 4432418449386195546L;
//	
//	private Logger logger = Logger.getLogger(ActivityRecommenderImpl.class);
//	
//	@Autowired private ActivityManager activityManager;
//	@Autowired private ESAssociationRulesSearch esAssociationRulesSearch;
//	
//	@Override
//	public List<Activity> getRecommendedActivitiesForCompetence(long competenceId, List<ActivityWallData> ignoreActivities, int limit) {
//		List<Activity> activities = new ArrayList<Activity>();
//	 
//		try {
//			List<ActivityAccessCount> frequentActivities = null;
//			
//			if (ignoreActivities.isEmpty()) {
//				frequentActivities = esAssociationRulesSearch.findFrequentCompetenceActivities(competenceId, limit);
//			} else {
//				List<Long> alreadyAddedActivities = new ArrayList<Long>();
//				
//				for (ActivityWallData awData : ignoreActivities) {
//					alreadyAddedActivities.add(awData.getId());
//				}
//				
//				frequentActivities = esAssociationRulesSearch.findMatchingActivitiesForCompetenceInAssociationRules(alreadyAddedActivities,
//						competenceId, limit);
//			}
//			
//			for (ActivityAccessCount count : frequentActivities) {
//				try {
//					Activity activity=activityManager.loadResource(Activity.class,count.getActivityId());
//					activities.add(activity);
//				} catch (ResourceCouldNotBeLoadedException e) {
//					logger.error(e);
//				}
//			}
//		} catch (IndexingServiceNotAvailable e) {
//			logger.warn(e);
//		}
//		return activities;
//	}
//
//	@Override
//	public List<Activity> getRelatedActivities(long competenceId, long activityId, int limit) {
//		List<Activity> relatedActivities = new ArrayList<Activity>();
//		
//		try {
//			List<Long> alreadyAddedActivities = new ArrayList<Long>();
//			List<ActivityAccessCount> relatedActivitiesCounters;
//			
//			relatedActivitiesCounters = esAssociationRulesSearch.findRelatedActivitiesForCompetenceAndActivityInAssociationRules(
//					alreadyAddedActivities, competenceId, activityId, limit);
//			
//			for (ActivityAccessCount count : relatedActivitiesCounters) {
//				try {
//					Activity activity = activityManager.loadResource(Activity.class, count.getActivityId());
//					relatedActivities.add(activity);
//				} catch (ResourceCouldNotBeLoadedException e) {
//					logger.error(e);
//				}
//			}
//		} catch (IndexingServiceNotAvailable e) {
//			logger.error(e);
//		}
//		return relatedActivities;
//	}
}
