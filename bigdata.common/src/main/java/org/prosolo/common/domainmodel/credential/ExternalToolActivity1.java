package org.prosolo.common.domainmodel.credential;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;

@Entity
public class ExternalToolActivity1 extends Activity1 {

	private static final long serialVersionUID = -3323104361100438967L;
	
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;
	private boolean openInNewWindow;
	private ScoreCalculation scoreCalculation;
	
	public ExternalToolActivity1() {
		
	}
	
	@Override
	public void accept(ActivityVisitor visitor) {
		visitor.visit(this);
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

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isAcceptGrades() {
		return acceptGrades;
	}
	
	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
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
