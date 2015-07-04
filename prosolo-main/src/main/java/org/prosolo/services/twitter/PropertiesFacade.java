package org.prosolo.services.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
public class PropertiesFacade {
//	private static Map<String, String> PROPERTIES = null;
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final private String TWITTER_CONSUMER_KEY = "twitter.consumer.key";
	final private String TWITTER_COnSUMER_SECRET = "twitter.consumer.secret";
	final private String TWITTER_ACCESS_TOKEN_KEY = "twitter.access.token";
	final private String TWITTER_ACCESS_TOKEN_SECRET = "twitter.access.token.secret";
	
	
	private Properties getProperties() throws IllegalArgumentException,	IOException {
		// String propertiesFile = PROPERTIES.get(propertyKey);
		String pathOfTwitterProperties = "config/twitter4j.properties";
		Properties properties = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathOfTwitterProperties);
		properties.load(is);
		is.close();
		// this.logger.info("Loaded properties of '{}' with: {}", propertyKey, properties);
		return properties;
	}

	public TwitterSiteProperties getTwitterSiteProperties(int accountId) throws IllegalArgumentException, IOException {
		Properties properties = getProperties();
		String consumerKey = properties.getProperty(TWITTER_CONSUMER_KEY);
		String consumerSecret = properties.getProperty(TWITTER_COnSUMER_SECRET);
		String accessToken = properties.getProperty(TWITTER_ACCESS_TOKEN_KEY+accountId);
		String accessTokenSecret = properties.getProperty(TWITTER_ACCESS_TOKEN_SECRET+accountId);

		TwitterSiteProperties twitterSiteProperties = new TwitterSiteProperties(
				consumerKey, consumerSecret, accessToken, accessTokenSecret);
		
		return twitterSiteProperties;
	}
	
}
