package org.prosolo.web.lti.message.extract;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.message.LTILaunchMessage;

import javax.servlet.http.HttpServletRequest;

public class Lti1LaunchMessageBuilder extends LtiLaunchMessageBuilder {

	@Override
	protected LTILaunchMessage getLtiLaunchMessageSpecific(HttpServletRequest request) throws Exception {
		LTILaunchMessage msg = new LTILaunchMessage();
		msg.setUserFirstName(request.getParameter(LTIConstants.LIS_PERSON_NAME_GIVEN));
		msg.setUserLastName(request.getParameter(LTIConstants.LIS_PERSON_NAME_FAMILY));
		msg.setUserEmail(request.getParameter(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY));
		msg.setUserID(request.getParameter(LTIConstants.USER_ID));
		return msg;
	}

}
