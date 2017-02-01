package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class ExternalToolTargetActivity1 extends TargetActivity1 {

	private static final long serialVersionUID = 2641471797643328969L;
	
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean openInNewWindow;
	private ScoreCalculation scoreCalculation;
	
	public ExternalToolTargetActivity1() {
		
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
	
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
	}
	
	@Enumerated(EnumType.STRING)
	public ScoreCalculation getScoreCalculation() {
		return scoreCalculation;
	}

	public void setScoreCalculation(ScoreCalculation scoreCalculation) {
		this.scoreCalculation = scoreCalculation;
	}
}
