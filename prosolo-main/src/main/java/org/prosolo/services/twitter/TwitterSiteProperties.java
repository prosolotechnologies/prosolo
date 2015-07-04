package org.prosolo.services.twitter;

/**
 * @author Zoran Jeremic 2013-08-11
 * 
 */
public class TwitterSiteProperties {

	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessTokenSecret;

	public TwitterSiteProperties(String cKey, String cSecret, String aToken, String aTokenSecret) {
		consumerKey = cKey;
		consumerSecret = cSecret;
		accessToken = aToken;
		accessTokenSecret = aTokenSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

}
