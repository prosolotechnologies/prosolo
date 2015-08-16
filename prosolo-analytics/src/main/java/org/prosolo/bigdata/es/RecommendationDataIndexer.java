package org.prosolo.bigdata.es;

import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;

/**
 * @author Zoran Jeremic Jun 2, 2015
 *
 */

public interface RecommendationDataIndexer {

	void updateMostActiveUsersForLearningGoal(
			MostActiveUsersForLearningGoal counterObject);

}
