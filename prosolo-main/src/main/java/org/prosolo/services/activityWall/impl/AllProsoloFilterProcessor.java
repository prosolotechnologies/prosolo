package org.prosolo.services.activityWall.impl;

import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.interfacesettings.FilterType;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Jan 31, 2015
 *
 */
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.ALL_PROSOLO })
public class AllProsoloFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity socialActivity, User user, Filter filter) {
		// Ignore Twitter posts
		if (socialActivity instanceof TwitterPostSocialActivity) {
			return false;
		}
		
		// Ignore private posts
		VisibilityType visibility = socialActivity.getVisibility();
		// need to check maker for null as TwitterPostSocialActivity does not have maker set
		if (visibility.equals(VisibilityType.PUBLIC)){
			return true;
		}
		if (visibility.equals(VisibilityType.PRIVATE) && 
				socialActivity.getMaker()!= null && socialActivity.getMaker().getId() == user.getId()) {
			return true;
		}
		return false;
	}
	
}
