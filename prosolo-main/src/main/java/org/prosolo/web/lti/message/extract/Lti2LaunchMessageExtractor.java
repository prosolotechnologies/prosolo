package org.prosolo.web.lti.message.extract;

import java.util.List;

import org.prosolo.web.lti.LTIConfigLoader;
import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.lti.json.data.MessageParameter;
import org.prosolo.web.lti.json.data.ResourceHandler;
import org.prosolo.web.lti.json.data.ToolProxy;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.util.PageUtil;

public class Lti2LaunchMessageExtractor extends LtiLaunchMessageExtractor {

	@Override
	protected LTILaunchMessage getLtiLaunchMessageSpecific() throws Exception {
		ToolProxy tp = LTIConfigLoader.getInstance().loadToolProxy();
		ResourceHandler rh = tp.getToolProfile().getResourceHandler().get(0);
		LTILaunchMessage msg = new LTILaunchMessage();
		List<MessageParameter> parameters = rh.getMessage().get(0).getParameter();
		for (MessageParameter mp : parameters) {
			setParam(msg, mp);
		}
		return msg;

	}

	private void setParam(LTILaunchMessage msg, MessageParameter mp) throws Exception {
		String param = PageUtil.getPostParameter("custom_" + mp.getName());
		switch (mp.getParameterValue()) {
		case LTIConstants.LTI2_PERSON_FIST_NAME:
			msg.setUserFirstName(param);
			break;
		case LTIConstants.LTI2_PERSON_LAST_NAME:
			msg.setUserLastName(param);
			break;
		case LTIConstants.LTI2_PERSON_USER_ID:
			msg.setUserID(param);
			break;
		case LTIConstants.LTI2_PERSON_EMAIL:
			msg.setUserEmail(param);
			break;
		case LTIConstants.LTI2_RESULT_SOURCED_ID:
			msg.setResultSourcedId(param);
			break;
		case LTIConstants.LTI2_RESULT_URL:
			msg.setResultUrl(param);
			break;
		}

	}

}
