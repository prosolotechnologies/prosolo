package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.lti.message.ToolProxyRegistrationMessage;
import org.prosolo.web.util.PageUtil;

public class LtiTPRegistrationMessageBuilder extends LtiMessageBuilder{

	@Override
	protected LTIMessage getLtiMessageSpecific() throws Exception {
		ToolProxyRegistrationMessage msg = new ToolProxyRegistrationMessage();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setRegKey(PageUtil.getPostParameter(LTIConstants.REG_KEY));
		msg.setRegPassword(PageUtil.getPostParameter(LTIConstants.REG_PASSWORD));
		msg.setTcProfileURL(PageUtil.getPostParameter(LTIConstants.TC_PROFILE_URL));
		return msg;
	}

}
