package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;

/**
@author Zoran Jeremic Jan 25, 2015
 *
 */

public interface SocialActivityFiltering {

	void checkSocialActivity(SocialActivity1 socialActivity, Session session);

}

