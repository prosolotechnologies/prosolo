package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.util.page.PageUtil;

public abstract class LtiLaunchMessageBuilder extends LtiMessageBuilder{

	@Override
	public LTIMessage getLtiMessageSpecific() throws Exception{
		LTILaunchMessage msg = getLtiLaunchMessageSpecific();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setConsumerKey(PageUtil.getPostParameter(LTIConstants.OAUTH_CONSUMER_KEY));
		msg.setRoles(PageUtil.getPostParameter(LTIConstants.ROLES));

		return msg;
	}

	protected abstract LTILaunchMessage getLtiLaunchMessageSpecific() throws Exception;
}
