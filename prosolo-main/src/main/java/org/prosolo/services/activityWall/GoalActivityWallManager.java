package org.prosolo.services.activityWall;

import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.activitywall.old.SocialActivityNotification;

public interface GoalActivityWallManager {

	/**
	 * @param user
	 * @param goal
	 * @param page
	 * @param limit
	 * @param loadOneMore
	 * @return
	 */
	List<SocialActivityNotification> getGoalSocialEvents(long userId, long goalId, int page, int limit, long filterUserId, boolean loadOneMore);
	
	/**
	 * @param userId
	 * @param goal
	 * @param page
	 * @param limit
	 * @param lastCheck
	 * @param loadOneMore
	 * @return
	 */
	List<SocialActivityNotification> getGoalSocialEvents(long userId, long goalId, int offset, int limit, Date lastCheck, long filterUserId, boolean loadOneMore);

}
