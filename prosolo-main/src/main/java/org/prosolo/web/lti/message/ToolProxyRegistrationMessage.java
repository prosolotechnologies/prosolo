package org.prosolo.web.lti.message;

import java.util.List;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.validator.EmptyValidator;
import org.prosolo.web.lti.validator.EqualValuesValidator;
import org.prosolo.web.lti.validator.NullValidator;

public class ToolProxyRegistrationMessage extends LTIMessage{
	
	private LtiMessageParameter regKey;
	private LtiMessageParameter regPassword;
	private LtiMessageParameter tcProfileURL;
	//private LtiMessageParameter userID;
	private LtiMessageParameter launchPresentationReturnURL;
	private LtiMessageParameter messageType;
	
	public ToolProxyRegistrationMessage(){
		regKey = new LtiMessageParameter(new NullValidator(new EmptyValidator(null)));
		regPassword = new LtiMessageParameter(new NullValidator(new EmptyValidator(null)));
		tcProfileURL = new LtiMessageParameter(new NullValidator(new EmptyValidator(null)));
		messageType = new LtiMessageParameter(new NullValidator(new EmptyValidator(
				new EqualValuesValidator(null, LTIConstants.MESSAGE_TYPE_TPREGISTRATION))));
		launchPresentationReturnURL = new LtiMessageParameter(new NullValidator(new EmptyValidator(null)));
	}
	
	public String getRegKey() {
		return regKey.getParameter();
	}
	public void setRegKey(String regKey) throws Exception {
		this.regKey.setParameter(regKey);
	}
	public String getRegPassword() {
		return regPassword.getParameter();
	}
	public void setRegPassword(String regPassword) throws Exception {
		this.regPassword.setParameter(regPassword);
	}
	public String getTcProfileURL() {
		return tcProfileURL.getParameter();
	}
	public void setTcProfileURL(String tcProfileURL) throws Exception {
		this.tcProfileURL.setParameter(tcProfileURL);
	}
	public String getLaunchPresentationReturnURL() {
		return launchPresentationReturnURL.getParameter();
	}

	public void setLaunchPresentationReturnURL(String launchPresentationReturnURL) throws Exception {
		this.launchPresentationReturnURL.setParameter(launchPresentationReturnURL);
	}

	public String getMessageType() {
		return messageType.getParameter();
	}

	public void setMessageType(String messageType) throws Exception {
		this.messageType.setParameter(messageType);
	}
	
}
