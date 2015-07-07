package org.prosolo.services.interaction;

import java.util.List;

import org.prosolo.common.domainmodel.activities.TargetActivity;

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

	//void testCreateTargetCompetenceActivitiesAnalyticalData();

}

