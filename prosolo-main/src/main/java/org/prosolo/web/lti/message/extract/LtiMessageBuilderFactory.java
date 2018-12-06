package org.prosolo.web.lti.message.extract;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;

import javax.servlet.http.HttpServletRequest;

public class LtiMessageBuilderFactory {

	public static LtiMessageBuilder createMessageExtractor(HttpServletRequest request) throws Exception{
		String msgType = request.getParameter(LTIConstants.MESSAGE_TYPE);
		if (LTIConstants.MESSAGE_TYPE_LTILAUNCH.equals(msgType)) {
			String version = request.getParameter(LTIConstants.LTI_VERSION);
			switch (version) {
				case LTIConstants.LTI_VERSION_ONE:
					return new Lti1LaunchMessageBuilder();
				case LTIConstants.LTI_VERSION_TWO:
					return new Lti2LaunchMessageBuilder();
			    default:
			    	throw new Exception("Required parameter missing");		
			}
		} else {
			if (LTIConstants.MESSAGE_TYPE_TPREGISTRATION.equals(msgType)) {
				return new LtiTPRegistrationMessageBuilder();
			} else {
				throw new Exception("Required parameter missing");
			}
		}
	}
}
