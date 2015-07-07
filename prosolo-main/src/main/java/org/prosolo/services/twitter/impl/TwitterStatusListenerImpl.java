package org.prosolo.services.twitter.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.twitter.TwitterPostsFactory;
import org.prosolo.services.twitter.TwitterStatusListener;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.util.TwitterUtils;
import org.springframework.stereotype.Service;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

 

/**
 * @author Zoran Jeremic 2013-08-11
 *
 */
@Service("org.prosolo.services.twitter.TwitterStatusListener")
public class TwitterStatusListenerImpl implements StatusListener, TwitterStatusListener {

	private static Logger logger = Logger.getLogger(TwitterHashtagsStatusConsumer.class);
	@Override
	public void onStatus(Status status) {
		twitter4j.User twitterUser = status.getUser();
		User user = ServiceLocator.getInstance().getService(UserOauthTokensManager.class).getUserByTwitterUserId(twitterUser.getId());
		
		System.out.println("TwitterStatusListener processing onStatus event:"
				+ status.getText()
				+ " at:"
				+ status.getCreatedAt()
				+ " from:"
				+ twitterUser.getId() + " / " + twitterUser.getScreenName());
		
		if (user != null) {
			String twitterStatus = StringUtils.getUTF(status.getText());
			twitterStatus = TwitterUtils.parseTweetText(twitterStatus);
	
			logger.info("User "+user+" posted a new tweet "+twitterStatus);
			
			ServiceLocator.getInstance().getService(TwitterPostsFactory.class).postStatusFromTwitter(
					user,
					status);
			
			logger.info("TwitterStatusListener processing onStatus event:"
					+ status.getText()
					+ " at:"
					+ status.getCreatedAt()
					+ " from:"
					+ twitterUser.getId() + " / " + twitterUser.getScreenName());
		} else {
			logger.info("Could not find ProSolo user that has a twitter account: " + twitterUser.getScreenName());
		}
	}
	 
	@Override
	public void onException(Exception arg0) { }

	@Override
	public void onDeletionNotice(StatusDeletionNotice arg0) { }

	@Override
	public void onScrubGeo(long arg0, long arg1) { }

	@Override
	public void onStallWarning(StallWarning arg0) { }

	@Override
	public void onTrackLimitationNotice(int arg0) { }

	public void close() { }
}
