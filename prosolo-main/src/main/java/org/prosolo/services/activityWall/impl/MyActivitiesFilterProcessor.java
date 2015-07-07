package org.prosolo.services.activityWall.impl;

import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Jan 27, 2015
 *
 */
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.MY_ACTIVITIES })
public class MyActivitiesFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity socialActivity, User user, Filter filter) {
		if (socialActivity instanceof TwitterPostSocialActivity) {
			return false;
		}
		
		// need to check maker for null as TwitterPostSocialActivity does not have maker set
		if (socialActivity.getMaker() != null && socialActivity.getMaker().getId() == user.getId()) {
			
			return true;
		}
		return false;
	}
	
}
