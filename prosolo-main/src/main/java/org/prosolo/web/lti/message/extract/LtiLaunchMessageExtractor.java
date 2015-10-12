package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.util.PageUtil;

public abstract class LtiLaunchMessageExtractor extends LtiMessageExtractor{

	@Override
	public LTIMessage getLtiMessageSpecific() throws Exception{
		LTILaunchMessage msg = getLtiLaunchMessageSpecific();
		msg.setMessageType(PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE));
		msg.setLaunchPresentationReturnURL(PageUtil.getPostParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL));
		msg.setConsumerKey(PageUtil.getPostParameter(LTIConstants.OAUTH_CONSUMER_KEY));

		return msg;
	}

	protected abstract LTILaunchMessage getLtiLaunchMessageSpecific() throws Exception;
}
