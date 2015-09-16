package org.prosolo.web.lti.message;

import java.util.ArrayList;
import java.util.List;

public class LTILaunchMessage extends LTIMessage{

	private String consumerKey;
	private String contextID;
	private String contextType;
	private String resourceLinkID;
	private List<String> roleScopeMentorList;
	private String toolConsumerInstanceGUID;
	private String contextTitle;
	private String contextLabel;
	private String resourceLinkTitle;
	private String resourceLinkDescription;
	private String userFirstName;
	private String userLastName;
	private String userFullName;
	private String userEmail;
	private String userImage;
	private String toolConsumerInstanceName;
	
	public LTILaunchMessage(){
		roleScopeMentorList = new ArrayList<>();
	}
	
	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getContextID() {
		return contextID;
	}
	public void setContextID(String contextID) {
		this.contextID = contextID;
	}
	public String getContextType() {
		return contextType;
	}
	public void setContextType(String contextType) {
		this.contextType = contextType;
	}
	public String getResourceLinkID() {
		return resourceLinkID;
	}
	public void setResourceLinkID(String resourceLinkID) {
		this.resourceLinkID = resourceLinkID;
	}
	public List<String> getRoleScopeMentorList() {
		return roleScopeMentorList;
	}
	public void setRoleScopeMentorList(List<String> roleScopeMentorList) {
		this.roleScopeMentorList = roleScopeMentorList;
	}
	public String getToolConsumerInstanceGUID() {
		return toolConsumerInstanceGUID;
	}
	public void setToolConsumerInstanceGUID(String toolConsumerInstanceGUID) {
		this.toolConsumerInstanceGUID = toolConsumerInstanceGUID;
	}
	public String getContextTitle() {
		return contextTitle;
	}
	public void setContextTitle(String contextTitle) {
		this.contextTitle = contextTitle;
	}
	public String getContextLabel() {
		return contextLabel;
	}
	public void setContextLabel(String contextLabel) {
		this.contextLabel = contextLabel;
	}
	public String getResourceLinkTitle() {
		return resourceLinkTitle;
	}
	public void setResourceLinkTitle(String resourceLinkTitle) {
		this.resourceLinkTitle = resourceLinkTitle;
	}
	public String getResourceLinkDescription() {
		return resourceLinkDescription;
	}
	public void setResourceLinkDescription(String resourceLinkDescription) {
		this.resourceLinkDescription = resourceLinkDescription;
	}
	public String getUserFirstName() {
		return userFirstName;
	}
	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}
	public String getUserLastName() {
		return userLastName;
	}
	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}
	public String getUserFullName() {
		return userFullName;
	}
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserImage() {
		return userImage;
	}
	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}
	public String getToolConsumerInstanceName() {
		return toolConsumerInstanceName;
	}
	public void setToolConsumerInstanceName(String toolConsumerInstanceName) {
		this.toolConsumerInstanceName = toolConsumerInstanceName;
	}
	
	
	
}
