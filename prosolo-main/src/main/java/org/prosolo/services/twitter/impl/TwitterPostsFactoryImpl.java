package org.prosolo.services.twitter.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.content.TwitterPost;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.AnonUser;
import org.prosolo.domainmodel.user.ServiceType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.UserType;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.twitter.TwitterPostsFactory;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import twitter4j.Status;

/**
 *
 * @author Zoran Jeremic, Sep 1, 2014
 *
 */
@Service("org.prosolo.services.twitter.TwitterPostsFactory")
public class TwitterPostsFactoryImpl implements TwitterPostsFactory {
	
	private static Logger logger = Logger.getLogger(TwitterPostsFactory.class);
	
	@Autowired private PostManager postManager;
	@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Autowired private UserOauthTokensManager userOauthTokensManager;
	@Autowired private EventFactory eventFactory;
	
	@Override
	@Transactional
	public void postStatusFromHashtagListener(Status tweet, List<String> twitterHashtags) {
		twitter4j.User twitterUser = tweet.getUser();

		long twitterId = twitterUser.getId();
		String creatorName = twitterUser.getName();
		String screenName = twitterUser.getScreenName();
		String profileUrl = "https://twitter.com/"+screenName;
		String profileImage = twitterUser.getProfileImageURL();
		String text = tweet.getText();
		Date created = tweet.getCreatedAt();
		String postLink = "https://twitter.com/" + twitterUser.getScreenName() + "/status/" + tweet.getId();
		
		try {
			boolean isProSoloUser = twitterStreamsManager.hasTwitterAccount(twitterId);
	
			User poster = null;
			
			if (isProSoloUser) {
				poster = userOauthTokensManager.getUserByTwitterUserId(twitterUser.getId());
			} else {
				logger.debug("Saving Twitter post from user '" + screenName + "' with content: '" + text + "'");
				
				// this instance is not saved, it is being used as a 
				// transient object
				AnonUser anonUser = new AnonUser();
				anonUser.setName(creatorName);
				anonUser.setProfileUrl(profileUrl);
				anonUser.setNickname(screenName);
				anonUser.setAvatarUrl(profileImage);
				anonUser.setServiceType(ServiceType.TWITTER);
				anonUser.setUserType(UserType.TWITTER_USER);
				anonUser.setDateCreated(new Date());
				
				poster = anonUser;
			}

			if (poster != null) {
				createTwitterPost(poster, created, postLink, twitterId, creatorName, screenName, profileUrl, profileImage, text, twitterHashtags, false, true);
			} else {
				logger.error("Something is wrong here. There should have been ProSolo user with twitter account id: " + twitterUser.getId());
			}
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
	}
	
	private void createTwitterPost(User user, Date dateCreated, String postLink, long tweetId, String creatorName, String screenName, String userUrl, 
			String profileImage, String text, List<String> hashtags, boolean toSave, boolean hashtagListener) throws EventException {
		TwitterPost post = postManager.createNewTwitterPost(user, dateCreated, postLink, tweetId, creatorName, screenName, userUrl, profileImage, text, VisibilityType.PUBLIC, hashtags, toSave);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("source", hashtagListener ? "hashtagListener" : "statusListener");
		
		eventFactory.generateEvent(EventType.TwitterPost, user, post, parameters); 
	}

	@Override
	@Transactional
	public void postStatusFromTwitter(User user, Status tweet) {
		twitter4j.User twitterUser = tweet.getUser();

		long tweetId = twitterUser.getId();
		String creatorName = twitterUser.getName();
		String screenName = twitterUser.getScreenName();
		String profileUrl = "https://twitter.com/"+screenName;
		String profileImage = twitterUser.getProfileImageURL();
		String text = tweet.getText();
		Date created = tweet.getCreatedAt();
		String postLink = "https://twitter.com/" + twitterUser.getScreenName() + "/status/" + tweet.getId();
		
		try {
			createTwitterPost(user, created, postLink, tweetId, creatorName, screenName, profileUrl, profileImage, text, null, true, false);
		} catch (EventException e) {
			logger.error(e.getMessage());
		}
	}

}
