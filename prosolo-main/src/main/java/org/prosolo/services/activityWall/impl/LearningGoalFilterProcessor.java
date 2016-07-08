package org.prosolo.services.activityWall.impl;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;

/**
 * @author Zoran Jeremic Feb 16, 2015
 *
 */
@Deprecated
//@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.GOALS })
public class LearningGoalFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity1 socialActivity, long userId, Filter filter) {
		boolean shouldBeDisplayed = false;
//		LearningGoalFilter goalFilter = (LearningGoalFilter) filter;
//		
//		if (socialActivity instanceof GoalNoteSocialActivity) {
//			long goalTargetId = ((GoalNoteSocialActivity) socialActivity).getGoalTarget().getId();
//			
//			if (goalFilter.getGoalId() == goalTargetId || goalFilter.getTargetGoalId() == goalTargetId) {
//				return true;
//			}
//		}
		return shouldBeDisplayed;
	}
	
}
