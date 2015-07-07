/**
 * 
 */
package org.prosolo.services.stats.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.stats.ActivityStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.stats.ActivityStatistics")
public class ActivityStatisticsImpl extends AbstractManagerImpl implements ActivityStatistics {

	private static final long serialVersionUID = 2593538210185343880L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityStatisticsImpl.class);
	
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private ActivityManager activityManager;
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfLikes(Activity activity) {
		return likeManager.likeCount(activity);
	}
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfDislikes(Activity activity) {
		return dislikeManager.dislikeCount(activity);
	}
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfOngoingGoalsWithActivity(Activity activity) {
		String query =
				"SELECT cast(COUNT(DISTINCT user) as int) " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals lGoal " +
				"LEFT JOIN lGoal.targetCompetences tComp " +
				"LEFT JOIN tComp.targetActivities tActivity " +
				"LEFT JOIN tActivity.activity activity " +
				"WHERE activity = :activity";
		  
	  	return (Integer) persistence.currentManager().createQuery(query)
	  			.setEntity("activity", activity)
	  			.uniqueResult();
	}
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfCompletedGoalsWithActivity(Activity activity) {
		String query =
				"SELECT cast(COUNT(DISTINCT user) as int) " +
				"FROM Portfolio portfolio " +
				//"LEFT JOIN user.portfolio portfolio " +
				 "LEFT JOIN portfolio.user user "+
				"LEFT JOIN portfolio.competences achievedCompetence " +
				"LEFT JOIN achievedCompetence.targetCompetence tComp " +
				"LEFT JOIN tComp.targetActivities tActivity " +
				"LEFT JOIN tActivity.activity activity " +
				"WHERE activity = :activity";
		
		return (Integer) persistence.currentManager().createQuery(query)
				.setEntity("activity", activity)
				.uniqueResult();
	}

}
