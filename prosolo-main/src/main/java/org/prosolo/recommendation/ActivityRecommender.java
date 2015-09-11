package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.web.activitywall.data.ActivityWallData;

public interface ActivityRecommender {

	List<Activity> getRecommendedActivitiesForCompetence(long competenceId,
			List<ActivityWallData> activities, int limit);

	public List<Activity> getRelatedActivities(long competenceId, long activityId, int limit);
}
