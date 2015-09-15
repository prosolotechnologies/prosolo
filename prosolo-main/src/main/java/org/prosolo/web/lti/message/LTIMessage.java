package org.prosolo.web.lti.message;

import java.util.ArrayList;
import java.util.List;

public class LTIMessage {

	private String messageType;
	private String ltiVersion;
	private String userID;
	private List<String> roles;
	private String launchPresentationReturnURL;
	
	
	public LTIMessage(){
		roles = new ArrayList<>();
	}

	public String getMessageType() {
		return messageType;
	}


	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}


	public String getLtiVersion() {
		return ltiVersion;
	}


	public void setLtiVersion(String ltiVersion) {
		this.ltiVersion = ltiVersion;
	}


	public String getUserID() {
		return userID;
	}


	public void setUserID(String userID) {
		this.userID = userID;
	}


	public List<String> getRoles() {
		return roles;
	}


	public void setRoles(List<String> roles) {
		this.roles = roles;
	}


	public String getLaunchPresentationReturnURL() {
		return launchPresentationReturnURL;
	}


	public void setLaunchPresentationReturnURL(String launchPresentationReturnURL) {
		this.launchPresentationReturnURL = launchPresentationReturnURL;
	}

	

	
	
}
