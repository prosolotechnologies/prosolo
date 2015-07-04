package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.web.activitywall.data.ActivityWallData;

public interface ActivityRecommender {

	//List<Activity> recommendActivities(User user, long targetCompId, int limit);
	
	//boolean disableActivityRecommendations(long targetCompId);
//
//	List<Activity> getRecommendedActivitiesForCompetence(User user,
//			long compId, int limit);

	List<Activity> getRecommendedActivitiesForCompetence(long competenceId,
			List<ActivityWallData> activities, int limit);

	public List<Activity> getRelatedActivities(long competenceId, long activityId, int limit);
}
