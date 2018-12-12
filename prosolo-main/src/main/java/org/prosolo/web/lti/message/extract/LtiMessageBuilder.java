package org.prosolo.web.lti.message.extract;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.message.LTIMessage;

import javax.servlet.http.HttpServletRequest;

public abstract class LtiMessageBuilder {
	public LTIMessage getLtiMessage(HttpServletRequest request) throws Exception{
		LTIMessage msg = getLtiMessageSpecific(request);
		msg.setLtiVersion(request.getParameter(LTIConstants.LTI_VERSION));
		try {
			msg.setId(request.getParameter(LTIConstants.TOOL_ID));
		} catch(Exception ex) {
			//msg.setId("1");
			return null;
		}

		return msg;
	}

	protected abstract LTIMessage getLtiMessageSpecific(HttpServletRequest request) throws Exception;
}
