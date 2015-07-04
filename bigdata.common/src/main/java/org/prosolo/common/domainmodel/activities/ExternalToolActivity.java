package org.prosolo.common.domainmodel.activities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activities.Activity;

/**
@author Zoran Jeremic Dec 01, 2014
*
*/
@Entity
public class ExternalToolActivity extends Activity{

	private static final long serialVersionUID = 4999098891552648483L;

	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	
	@Column(name = "launchUrl")
	public String getLaunchUrl() {
		return launchUrl;
	}
	
	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}
	
	@Column(name = "sharedSecret")
	public String getSharedSecret() {
		return sharedSecret;
	}
	
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAcceptGrades() {
		return acceptGrades;
	}
	
	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
	}
	
	@Column(name = "consumerKey")
	public String getConsumerKey() {
		return consumerKey;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
	
}
