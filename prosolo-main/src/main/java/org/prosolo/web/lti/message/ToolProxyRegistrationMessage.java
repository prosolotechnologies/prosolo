package org.prosolo.web.lti.message;

public class ToolProxyRegistrationMessage extends LTIMessage{
	
	private String regKey;
	private String regPassword;
	private String tcProfileURL;
	
	
	public String getRegKey() {
		return regKey;
	}
	public void setRegKey(String regKey) {
		this.regKey = regKey;
	}
	public String getRegPassword() {
		return regPassword;
	}
	public void setRegPassword(String regPassword) {
		this.regPassword = regPassword;
	}
	public String getTcProfileURL() {
		return tcProfileURL;
	}
	public void setTcProfileURL(String tcProfileURL) {
		this.tcProfileURL = tcProfileURL;
	}
	
	
}
