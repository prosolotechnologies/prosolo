package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class ExternalToolActivity1 extends Activity1 {

	private static final long serialVersionUID = -3323104361100438967L;
	
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	
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
	
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAcceptGrades() {
		return acceptGrades;
	}
	
	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
	}
	
	public String getConsumerKey() {
		return consumerKey;
	}
	
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
}
