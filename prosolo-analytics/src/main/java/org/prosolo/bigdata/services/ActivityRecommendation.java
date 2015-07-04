package org.prosolo.bigdata.services;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;

/**
@author Zoran Jeremic Apr 20, 2015
 *
 */

public interface ActivityRecommendation {

 

	List<ActivityAccessCount> getRecommendedActivitiesForCompetence(
			Long competenceId, List<Long> ignoredActivities, int limit);

 

	List<ActivityAccessCount> getRelatedActivitiesForActivity(
			Long competenceId, Long activityId,
			List<Long> alreadyAddedActivities, int limit);

}

