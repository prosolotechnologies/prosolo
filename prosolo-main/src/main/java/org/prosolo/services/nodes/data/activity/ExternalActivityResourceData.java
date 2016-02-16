package org.prosolo.services.nodes.data.activity;

public class ExternalActivityResourceData extends ResourceData {

	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	
	public ExternalActivityResourceData() {
		setActivityType();
	}

	public ExternalActivityResourceData(String launchUrl, String sharedSecret, String consumerKey,
			boolean acceptGrades) {
		this.launchUrl = launchUrl;
		this.sharedSecret = sharedSecret;
		this.consumerKey = consumerKey;
		this.acceptGrades = acceptGrades;
		setActivityType();
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public boolean isAcceptGrades() {
		return acceptGrades;
	}

	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
	}

	@Override
	void setActivityType() {
		this.activityType = ActivityType.EXTERNAL_TOOL;
	}

}
