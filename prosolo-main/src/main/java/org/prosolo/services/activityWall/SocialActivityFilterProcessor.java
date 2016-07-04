package org.prosolo.services.activityWall;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.filters.Filter;

/**
 * @author Zoran Jeremic Jan 25, 2015
 *
 */

public interface SocialActivityFilterProcessor {
	
	public boolean checkSocialActivity(SocialActivity1 socialActivity, User user, Filter filter);
}
