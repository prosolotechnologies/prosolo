package org.prosolo.services.activityWall.impl;

import org.prosolo.domainmodel.activitywall.GoalNoteSocialActivity;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.interfacesettings.FilterType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.LearningGoalFilter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Feb 16, 2015
 *
 */
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.GOALS })
public class LearningGoalFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity socialActivity, User user, Filter filter) {
		boolean shouldBeDisplayed = false;
		LearningGoalFilter goalFilter = (LearningGoalFilter) filter;
		
		if (socialActivity instanceof GoalNoteSocialActivity) {
			long goalTargetId = ((GoalNoteSocialActivity) socialActivity).getGoalTarget().getId();
			
			if (goalFilter.getGoalId() == goalTargetId || goalFilter.getTargetGoalId() == goalTargetId) {
				return true;
			}
		}
		return shouldBeDisplayed;
	}
	
}
