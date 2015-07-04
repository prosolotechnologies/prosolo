package org.prosolo.services.twitter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.twitter.ProfanityFilter;
import org.prosolo.services.twitter.TwitterHashtagsQueueHandler;
import org.prosolo.services.twitter.TwitterPostsFactory;

import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * @author Zoran Jeremic 2013-10-14
 * 
 */

public class TwitterHashtagsStatusConsumer implements Runnable {
	
	private static Logger logger = Logger.getLogger(TwitterHashtagsStatusConsumer.class);
	
	public int CONSUME_INTERVAL;

	// private TwitterHashtagsQueueHandler twitterHashtagsQueueHandler;
	// private TwitterStreamsManager twitterStreamsManager;
	public TwitterHashtagsStatusConsumer() {
		CONSUME_INTERVAL = Settings.getInstance().config.twitterStreamConfig.postConsumeIntervalMiliseconds;
		// twitterHashtagsQueueHandler=ServiceLocator.getInstance().getService(TwitterHashtagsQueueHandler.class);
		// twitterStreamsManager=ServiceLocator.getInstance().getService(TwitterStreamsManager.class);
		Thread consumer = new Thread(this);
		consumer.start();
 
	}

	@Override
	public void run() {
 
		while (true) {
			try {
				Thread.sleep(CONSUME_INTERVAL);
			} catch (InterruptedException e) {
				logger.error(e);
			}
			
			// Status status= twitterHashtagsQueueHandler.dequeue();
			Status status = ServiceLocator.getInstance().getService(TwitterHashtagsQueueHandler.class).dequeue();
			Date createdAt = new Date(status.getCreatedAt().getTime());
			twitter4j.User twitterUser = status.getUser();
			//System.out.println("TWITTER:"+status.getText());
			logger.debug("TwitterHasttagsStatusConsumer processing onStatus event:"
					+ status.getText()
					+ " at:"
					+ createdAt
					+ " from:"
					+ twitterUser.getId() + " / " + twitterUser.getScreenName());

			HashtagEntity[] hashtagEntities = status.getHashtagEntities();
			List<String> hashtags = new ArrayList<String>();
			boolean hasBlacklistedTag = false;
			boolean hasProfanity=false;
			
			for (HashtagEntity htEntity : hashtagEntities) {
				String tagText = htEntity.getText().toLowerCase();
				if (ServiceLocator.getInstance().getService(TwitterHashtagsQueueHandler.class).isHashTagBlacklisted(tagText)) {
					hasBlacklistedTag = true;
				}
				hashtags.add(tagText);
			}
			try {
				String statusText = status.getText();
				statusText = statusText.replaceAll("[^\\x00-\\x7f-\\x80-\\xad]", "");
				hasProfanity = ServiceLocator.getInstance().getService(ProfanityFilter.class).detectProfanity(statusText);
					
				if (!hasBlacklistedTag && !hasProfanity) {
					ServiceLocator
							.getInstance()
							.getService(TwitterPostsFactory.class)
							.postStatusFromHashtagListener(
									status,
									hashtags);
				} 
			} catch (Exception exception) {
				logger.error("EXCEPTION IN THREAD:"	+ exception.getLocalizedMessage());
			}			 
		}
	}

}
