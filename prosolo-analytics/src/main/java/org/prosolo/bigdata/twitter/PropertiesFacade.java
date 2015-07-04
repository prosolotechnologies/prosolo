package org.prosolo.bigdata.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

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

	public TwitterSiteProperties getTwitterSiteProperties(int accountId) throws IllegalArgumentException, IOException, NotFoundException {
		Properties properties = getProperties();
		String consumerKey = properties.getProperty(TWITTER_CONSUMER_KEY);
		String consumerSecret = properties.getProperty(TWITTER_COnSUMER_SECRET);
		String accessToken = properties.getProperty(TWITTER_ACCESS_TOKEN_KEY+accountId);
		String accessTokenSecret = properties.getProperty(TWITTER_ACCESS_TOKEN_SECRET+accountId);
		if(accessToken==null){
			throw new NotFoundException();
		}
		TwitterSiteProperties twitterSiteProperties = new TwitterSiteProperties(
				consumerKey, consumerSecret, accessToken, accessTokenSecret);
		
		return twitterSiteProperties;
	}
	
	public Queue<TwitterSiteProperties> getAllTwitterSiteProperties() {
		 PropertiesFacade pFacade=new PropertiesFacade();
		 boolean hasmore=true;
		 int accountId=0;
		 Queue<TwitterSiteProperties> properties=new LinkedList<TwitterSiteProperties>();
		 while(hasmore){
			 try {
				TwitterSiteProperties twitterSiteProperties=pFacade.getTwitterSiteProperties(accountId);
				properties.add(twitterSiteProperties);
				System.out.println("found:"+twitterSiteProperties.getAccessToken()+" for account:"+accountId);
				accountId++;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				hasmore=false;
			}
			 
		 }
		return properties;
	}
	
}
