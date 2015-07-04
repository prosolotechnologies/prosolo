package org.prosolo.common.domainmodel.feeds;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.feeds.FeedsDigest;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Entity
public class SubscribedTwitterHashtagsFeedsDigest extends FeedsDigest {
	
	private static final long serialVersionUID = 547672228588253043L;
	
	private User feedsSubscriber;

	@OneToOne
	public User getFeedsSubscriber() {
		return feedsSubscriber;
	}

	public void setFeedsSubscriber(User feedsSubscriber) {
		this.feedsSubscriber = feedsSubscriber;
	}
}
