package org.prosolo.services.lti;

import java.util.List;

import org.prosolo.common.domainmodel.lti.LtiConsumer;

public interface LtiConsumerManager {

	//public LtiConsumer findValidConsumer(String key, long toolSetId, LtiVersion ltiVersion, HttpServletRequest request, String url) throws OauthException;
	public LtiConsumer registerLTIConsumer(long toolSetId, String key, String secret, List<String> capabilities, List<org.prosolo.web.lti.json.data.Service> services); 
}