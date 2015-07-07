/**
 * 
 */
package org.prosolo.services.stats;

import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Nikola Milikic
 * 
 */
public interface LearningGoalStatistics {

	long getAverageGoalCompletionTime(User user);

	long getGoalCompletionTime(LearningGoal goal);
}
