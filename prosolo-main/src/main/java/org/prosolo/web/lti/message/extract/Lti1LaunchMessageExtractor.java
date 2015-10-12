package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.util.PageUtil;

public class Lti1LaunchMessageExtractor extends LtiLaunchMessageExtractor{

	@Override
	protected LTILaunchMessage getLtiLaunchMessageSpecific() throws Exception {
		LTILaunchMessage msg = new LTILaunchMessage();
		msg.setUserFirstName(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_NAME_GIVEN));
		msg.setUserLastName(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_NAME_FAMILY));
		msg.setUserEmail(PageUtil.getPostParameter(LTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY));
		msg.setUserID(PageUtil.getPostParameter(LTIConstants.USER_ID));
		return msg;
	}

}
