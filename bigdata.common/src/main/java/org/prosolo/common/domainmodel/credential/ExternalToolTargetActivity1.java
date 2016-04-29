package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;

@Entity
public class ExternalToolTargetActivity1 extends TargetActivity1 {

	private static final long serialVersionUID = 2641471797643328969L;
	
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	
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
	
}
