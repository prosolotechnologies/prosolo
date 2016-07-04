/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityNotification;
import org.prosolo.common.domainmodel.activitywall.old.SocialStreamSubViewType;
import org.prosolo.services.activityWall.GoalActivityWallManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.activitywall.GoalActivityWallManager")
public class GoalActivityWallManagerImpl extends AbstractManagerImpl implements GoalActivityWallManager {
	
	private static final long serialVersionUID = -8724866052471011910L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(GoalActivityWallManager.class);
	
	@Override
	@Transactional(readOnly = true)
	public List<SocialActivityNotification> getGoalSocialEvents(long userId, long goalId, int offset, int limit, long filterUserId, boolean loadOneMore) {
		return getGoalSocialEvents(userId, goalId, offset, limit, null, filterUserId, loadOneMore);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<SocialActivityNotification> getGoalSocialEvents(long userId, long goalId, int offset, int limit, Date lastCheck, long filterUserId, boolean loadOneMore) {
		StringBuffer query = new StringBuffer(
			"SELECT DISTINCT activityNotification " +
			"FROM SocialActivityNotification activityNotification " +
			"LEFT JOIN activityNotification.user user " +
			"LEFT JOIN activityNotification.subViews subView " +
			"LEFT JOIN activityNotification.socialActivity socialActivity " +
			"LEFT JOIN subView.relatedResources relatedRes " +
			"WHERE user.id = :userId " +
				"AND " +
					"((relatedRes.class = TargetLearningGoal " +
						"AND relatedRes.learningGoal.id = :goalId) " +
					"OR " +
					"(relatedRes.class = LearningGoal " +
						"AND relatedRes.id = :goalId)) " +
				"AND activityNotification.hidden = false " +
				"AND socialActivity.deleted = false " +
				"AND socialActivity.action NOT IN (:excludedEvents) " +
				"AND subView.type = :subViewType " +
				"AND socialActivity.maker IN ( " +
					"SELECT DISTINCT collab "+
					"FROM User collab "+
					"LEFT JOIN collab.learningGoals tGoal "+
					"LEFT JOIN tGoal.learningGoal goal "+
					"WHERE goal.id = :goalId " + 
						"AND tGoal.deleted = false " +
				") "
		);
		
		if (lastCheck != null) {
			query.append(
				"AND socialActivity.lastAction > :lastCheck "
			);
		}
		
		if (filterUserId > 0) {
			query.append("AND socialActivity.maker.id = :filterUserId ");
		}
		
		query.append(
			"ORDER BY socialActivity.lastAction DESC"
		);
		
		Query q =  persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("goalId", goalId)
				.setString("subViewType", SocialStreamSubViewType.GOAL_WALL.name())
				.setParameterList("excludedEvents", new EventType[]{EventType.Comment});
		
		if (lastCheck != null) {
			q.setTimestamp("lastCheck", lastCheck);
		}
		
		if (filterUserId > 0) {
			q.setLong("filterUserId", filterUserId);
		}
		
		if (offset >= 0) {
			q.setFirstResult(offset);
		}
		if (limit > 0) {
			q.setMaxResults(loadOneMore ? limit+1 : limit);
		}
	 
		@SuppressWarnings("unchecked")
		List<SocialActivityNotification> activityNotifications =  q.list();
		return activityNotifications;
	}
	
}
