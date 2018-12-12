package org.prosolo.web.lti.message.extract;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.lti.message.ToolProxyRegistrationMessage;

import javax.servlet.http.HttpServletRequest;

public class LtiTPRegistrationMessageBuilder extends LtiMessageBuilder{

	@Override
	protected LTIMessage getLtiMessageSpecific(HttpServletRequest request) throws Exception {
		ToolProxyRegistrationMessage msg = new ToolProxyRegistrationMessage();
		msg.setMessageType(request.getParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLaunchPresentationReturnURL(request.getParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setRegKey(request.getParameter(LTIConstants.REG_KEY));
		msg.setRegPassword(request.getParameter(LTIConstants.REG_PASSWORD));
		msg.setTcProfileURL(request.getParameter(LTIConstants.TC_PROFILE_URL));
		return msg;
	}

}
