package org.prosolo.services.activityWall.impl;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
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
	public boolean checkSocialActivity(SocialActivity1 socialActivity, User user, Filter filter) {
		if (socialActivity instanceof TwitterPostSocialActivity1) {
			return false;
		}
		
		// need to check maker for null as TwitterPostSocialActivity does not have maker set
		if (socialActivity.getActor() != null && socialActivity.getActor().getId() == user.getId()) {
			
			return true;
		}
		return false;
	}
	
}
