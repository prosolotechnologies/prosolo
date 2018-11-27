package org.prosolo.web.lti.message.extract;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.LTIMessage;

import javax.servlet.http.HttpServletRequest;

public abstract class LtiLaunchMessageBuilder extends LtiMessageBuilder {

	@Override
	public LTIMessage getLtiMessageSpecific(HttpServletRequest request) throws Exception{
		LTILaunchMessage msg = getLtiLaunchMessageSpecific(request);
		msg.setMessageType(request.getParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLaunchPresentationReturnURL(request.getParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setConsumerKey(request.getParameter(LTIConstants.OAUTH_CONSUMER_KEY));
		msg.setRoles(request.getParameter(LTIConstants.ROLES));

		return msg;
	}

	protected abstract LTILaunchMessage getLtiLaunchMessageSpecific(HttpServletRequest request) throws Exception;
}
