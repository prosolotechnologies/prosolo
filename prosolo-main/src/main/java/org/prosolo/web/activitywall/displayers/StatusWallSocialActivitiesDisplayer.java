package org.prosolo.web.activitywall.displayers;

import java.util.List;

import org.prosolo.common.domainmodel.activitywall.old.SocialStreamSubViewType;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service
@Scope("prototype")
public class StatusWallSocialActivitiesDisplayer extends DefaultWallSocialActivitiesDisplayer {
	
	@Override
	protected List<SocialActivityData> fetchActivities(int offset, int limit) {
		List<SocialActivityData> socialActivities = activityWallManager.getSocialActivities(
				loggedUser.getId(),
				filter,
				offset, 
				limit);
		return socialActivities;
	}
	
	@Override
	protected SocialStreamSubViewType getSubViewType() {
		return SocialStreamSubViewType.STATUS_WALL;
	}
	
}
