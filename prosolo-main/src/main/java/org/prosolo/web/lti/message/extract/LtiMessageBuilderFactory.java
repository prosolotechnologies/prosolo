package org.prosolo.web.lti.message.extract;

import org.prosolo.web.lti.LTIConstants;
import org.prosolo.web.util.PageUtil;

public class LtiMessageBuilderFactory {

	public static LtiMessageBuilder createMessageExtractor() throws Exception{
		String msgType = PageUtil.getPostParameter(LTIConstants.MESSAGE_TYPE);
		if(LTIConstants.MESSAGE_TYPE_LTILAUNCH.equals(msgType)){
			String version = PageUtil.getPostParameter(LTIConstants.LTI_VERSION);
			switch(version){
				case LTIConstants.LTI_VERSION_ONE:
					return new Lti1LaunchMessageBuilder();
				case LTIConstants.LTI_VERSION_TWO:
					return new Lti2LaunchMessageBuilder();
			    default:
			    	throw new Exception("Required parameter missing");		
			}
		}else{
			if(LTIConstants.MESSAGE_TYPE_TPREGISTRATION.equals(msgType)){
				return new LtiTPRegistrationMessageBuilder();
			}else{
				throw new Exception("Requried parameter missing");
			}
		}
	}
}
