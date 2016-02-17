package org.prosolo.common.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;


//import org.prosolo.bigdata.twitter.NotFoundException;
//import org.prosolo.bigdata.twitter.TwitterSiteProperties;
import org.prosolo.common.config.CommonSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
public class PropertiesFacade {
//	private static Map<String, String> PROPERTIES = null;
	
	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final private String TWITTER_CONSUMER_KEY = "twitter.consumer.key";
	final private String TWITTER_CONSUMER_SECRET = "twitter.consumer.secret";
	final private String TWITTER_ACCESS_TOKEN_KEY = "twitter.access.token";
	final private String TWITTER_ACCESS_TOKEN_SECRET = "twitter.access.token.secret";
	
	
	private Properties getProperties() throws IllegalArgumentException,	IOException {
		// String propertiesFile = PROPERTIES.get(propertyKey);
		String pathOfTwitterProperties = "config/twitter4j"+CommonSettings.getInstance().config.getNamespaceSufix()+".properties";

		System.out.println("PATH:"+pathOfTwitterProperties);
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
		String consumerSecret = properties.getProperty(TWITTER_CONSUMER_SECRET);
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
		 Gson gson=new Gson();
		 while(hasmore){
			 try {
				TwitterSiteProperties twitterSiteProperties=pFacade.getTwitterSiteProperties(accountId);
				if(twitterSiteProperties.getAccessToken()!=null && twitterSiteProperties.getAccessToken().length()>10){
					properties.add(twitterSiteProperties);
					accountId++;
				}				
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				hasmore=false;
			}
			 
		 }
		return properties;
	}
	
}
