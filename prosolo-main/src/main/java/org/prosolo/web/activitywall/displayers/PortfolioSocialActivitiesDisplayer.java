package org.prosolo.web.activitywall.displayers;

import java.util.List;
import java.util.Locale;

import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.filters.Filter;
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
public class PortfolioSocialActivitiesDisplayer extends DefaultWallSocialActivitiesDisplayer {
	
	private long wallOwner;
	
	public void init(User loggedUser, Locale locale, Filter filter, long wallOwner) {
		super.init(loggedUser, locale, filter);
		this.wallOwner = wallOwner; 
	}
	
	@Override
	protected List<SocialActivityData> fetchActivities(int offset, int limit) {
		return activityWallManager.getUserPublicSocialEvents(
				wallOwner, 
				offset, 
				limit);
	}
	
	@Override
	protected SocialStreamSubViewType getSubViewType() {
		return SocialStreamSubViewType.STATUS_WALL;
	}
}
