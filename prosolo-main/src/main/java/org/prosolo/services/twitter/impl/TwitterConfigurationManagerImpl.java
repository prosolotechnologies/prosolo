package org.prosolo.services.twitter.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.prosolo.common.twitter.NotFoundException;
import org.prosolo.common.twitter.PropertiesFacade;
//import org.prosolo.services.twitter.PropertiesFacade;
import org.prosolo.services.twitter.TwitterConfigurationManager;
import org.prosolo.common.twitter.TwitterSiteProperties;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
/**
 * @author Zoran Jeremic 2014-04-02
 *
 */

@Service("org.prosolo.services.twitter.TwitterConfigurationManager")
public class TwitterConfigurationManagerImpl implements TwitterConfigurationManager {
	
	private static Logger logger = Logger.getLogger(TwitterConfigurationManagerImpl.class);
 
	private Twitter twitter = null;
	
	public TwitterConfigurationManagerImpl(){ }
	
	public ConfigurationBuilder createTwitterConfigurationBuilder(){
		TwitterSiteProperties properties = null;
		try {
			properties = new PropertiesFacade().getTwitterSiteProperties(0);
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (NotFoundException e) {
			logger.error(e);
		}
		
		String consumerKey = properties.getConsumerKey();
		String consumerSecret = properties.getConsumerSecret();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAuthenticationURL("https://api.twitter.com/oauth/request_token");
		cb.setOAuthAccessTokenURL("https://api.twitter.com/oauth/access_token");
		cb.setOAuthAuthorizationURL("https://api.twitter.com/oauth/authorize");
		cb.setOAuthRequestTokenURL("https://api.twitter.com/oauth/request_token");
		cb.setRestBaseURL("https://api.twitter.com/1.1/");
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		return cb;
	}

	@Override
	public Twitter getTwitterInstance(){
		if (twitter == null) {
			twitter = new TwitterFactory(createTwitterConfigurationBuilder().build()).getInstance();
		}
		return twitter;
	}

}
