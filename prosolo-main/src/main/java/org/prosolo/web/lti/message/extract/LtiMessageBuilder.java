package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.util.page.PageUtil;

public abstract class LtiMessageBuilder {
	public LTIMessage getLtiMessage() throws Exception{
		LTIMessage msg = getLtiMessageSpecific();
		msg.setLtiVersion(PageUtil.getPostParameter(LTIConstants.LTI_VERSION));
		msg.setId(PageUtil.getPostParameter(LTIConstants.TOOL_ID));
		return msg;
	}

	protected abstract LTIMessage getLtiMessageSpecific() throws Exception;
}
