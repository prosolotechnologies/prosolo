package org.prosolo.services.activityWall.impl;


import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.MyNetworkFilter;
import org.prosolo.services.activityWall.strategy.Strategy;

/**
@author Zoran Jeremic Jan 27, 2015
 *
 */
@Strategy(type=SocialActivityFilterProcessor.class, filters={FilterType.MY_NETWORK})
public class MyNetworkFilterProcessor  implements SocialActivityFilterProcessor{

	@Override
	public boolean checkSocialActivity(SocialActivity1 socialActivity, User user,
			Filter filter) {
		
//		VisibilityType visibility = socialActivity.getVisibility();
//		if (visibility.equals(VisibilityType.PRIVATE) && socialActivity.getMaker().getId() != user.getId()) {
//			return false;
//		}
		
		MyNetworkFilter myNetworkFilter = (MyNetworkFilter) filter;
		
		// need to check maker for null as TwitterPostSocialActivity does not have maker set
		if (socialActivity.getActor() != null && myNetworkFilter.isContainsUsersId(socialActivity.getActor().getId())) {
			return true;
		}
		return false;
	}

}

