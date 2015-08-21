package org.prosolo.services.interaction;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;

/**
@author Zoran Jeremic Apr 12, 2015
 *
 */

public interface AnalyticalServiceCollector {

	void increaseUserActivityLog(long userid, long daysSinceEpoch);

	void createActivityInteractionData(long id, long id2);

	//void testCreateActivityInteractionData();

 

	void createTargetCompetenceActivitiesData(long competenceId,
			long targetCompetenceId, List<TargetActivity> tActivities);

	void increaseUserActivityForLearningGoalLog(long userid, long learningGoal,
			long daysSinceEpoch);

	void sendUpdateHashtagsMessage(Map<String, String> parameters, long goalId,
			long userId);
	
	void updateTwitterUser(long userId, boolean addUser);

	void increaseUserEventCount(EventType event, long daysSinceEpoch);

}

