package org.prosolo.services.feeds.data;

import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class UserFeedSourceAggregate {
	
	private User user;
	private FeedSource feedSource;
	private boolean included = true;
	
	public UserFeedSourceAggregate(User user, FeedSource feedSource, boolean included) {
		this.user = user;
		this.feedSource = feedSource;
		this.included = included;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public FeedSource getFeedSource() {
		return feedSource;
	}
	
	public void setFeedSource(FeedSource feedSource) {
		this.feedSource = feedSource;
	}
	
	public boolean isIncluded() {
		return included;
	}
	
	public void setIncluded(boolean included) {
		this.included = included;
	}
	
}
