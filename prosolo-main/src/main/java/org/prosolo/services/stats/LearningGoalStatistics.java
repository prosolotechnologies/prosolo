/**
 * 
 */
package org.prosolo.services.stats;

import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;

/**
 * @author Nikola Milikic
 * 
 */
public interface LearningGoalStatistics {

	long getAverageGoalCompletionTime(User user);

	long getGoalCompletionTime(LearningGoal goal);
}
