/**
 * 
 */
package org.prosolo.services.stats.impl;


import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.stats.LearningGoalStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.stats.LearningGoalStatistics")
public class LearningGoalStatisticsImpl extends AbstractManagerImpl
	implements LearningGoalStatistics {

	private static final long serialVersionUID = 1119817012384536027L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LearningGoalStatistics.class);
	
	@Override
	@Transactional (readOnly = true)
	public long getAverageGoalCompletionTime(User user) {
		String query = 
				"SELECT completedGoal.completedDate, goal.dateCreated " +
				"FROM Portfolio portfolio " +
				"LEFT JOIN portfolio.user user "+
				"LEFT JOIN portfolio.completedGoals completedGoal " +
				"LEFT JOIN completedGoal.goal goal " +
				"WHERE user = :user";
		
		@SuppressWarnings({ "unchecked" })
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setEntity("user", user).
				list();
		
		if (result != null && !result.isEmpty()) {
			int totalDiffs = 0;
					
			for (Object[] res : result) {
				Timestamp timeCompleted = (Timestamp) res[0];
				Timestamp timeStarted = (Timestamp) res[1];
				
				if (timeCompleted != null && timeStarted != null)
					totalDiffs += timeCompleted.getTime() - timeStarted.getTime();
			}
			
			return totalDiffs/result.size();
		}
		
		return 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getGoalCompletionTime(LearningGoal goal) {
		String query = 
			"SELECT completedGoal.completedDate, goal.dateCreated " +
			"FROM CompletedGoal completedGoal " +
			"LEFT JOIN completedGoal.targetGoal targetGoal " +
			"LEFT JOIN targetGoal.learningGoal goal " +
			"WHERE goal = :goal";
		
		@SuppressWarnings({ "unchecked" })
		List<Object[]> result = persistence.currentManager().createQuery(query).
				setEntity("goal", goal).
				list();
		
		if (result != null && !result.isEmpty()) {
			int totalDiffs = 0;
					
			for (Object[] res : result) {
				Timestamp timeCompleted = (Timestamp) res[0];
				Timestamp timeStarted = (Timestamp) res[1];
				
				if (timeCompleted != null && timeStarted != null)
					totalDiffs += timeCompleted.getTime() - timeStarted.getTime();
			}
			
			return totalDiffs/result.size();
		}
		
		return 0;
	}
}
