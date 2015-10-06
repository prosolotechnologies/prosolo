package org.prosolo.services.lti;

import org.prosolo.common.domainmodel.lti.LtiConsumer;

public interface LtiConsumerManager {

	//public LtiConsumer findValidConsumer(String key, long toolSetId, LtiVersion ltiVersion, HttpServletRequest request, String url) throws OauthException;
	public LtiConsumer registerLTIConsumer(LtiConsumer consumer, long toolSetId); 
}