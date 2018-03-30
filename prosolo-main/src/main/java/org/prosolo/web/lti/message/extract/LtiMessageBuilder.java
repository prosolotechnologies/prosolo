package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.message.LTIMessage;
import org.prosolo.web.util.page.PageUtil;

public abstract class LtiMessageBuilder {
	public LTIMessage getLtiMessage() throws Exception{
		LTIMessage msg = getLtiMessageSpecific();
		msg.setLtiVersion(PageUtil.getPostParameter(LTIConstants.LTI_VERSION));
		try{
			msg.setId(PageUtil.getGetParameter(LTIConstants.TOOL_ID));
		}catch(Exception ex){
			//msg.setId("1");
			return null;
		}



		return msg;
	}

	protected abstract LTIMessage getLtiMessageSpecific() throws Exception;
}
