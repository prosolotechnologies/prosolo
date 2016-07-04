package org.prosolo.services.activityWall.impl;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
 * @author Zoran Jeremic Jan 25, 2015
 *
 */
// @Service("org.prosolo.services.activitystream.AllFilterProcessor")
@Strategy(type = SocialActivityFilterProcessor.class, filters = { FilterType.ALL })
public class AllFilterProcessor implements SocialActivityFilterProcessor {
	
	@Override
	public boolean checkSocialActivity(SocialActivity1 socialActivity, User user, Filter filter) {
		// TODO Auto-generated method stub
		// Ignore private posts
		// Ignore private posts
		//VisibilityType visibility = socialActivity.getVisibility();
		// need to check maker for null as TwitterPostSocialActivity does not have maker set
//		if (visibility.equals(VisibilityType.PUBLIC)){
//			return true;
//		}
//		if (visibility.equals(VisibilityType.PRIVATE) && 
//				socialActivity.getMaker()!= null && socialActivity.getMaker().getId() == user.getId()) {
//			return true;
//		}
//		
//		return false;
		
		return true;
	}
	
}
