package org.prosolo.services.lti;

import java.util.List;

import org.prosolo.common.domainmodel.lti.LtiConsumer;

public interface LtiConsumerManager {

	public LtiConsumer registerLTIConsumer(long toolSetId, String key, String secret, List<String> capabilities, List<org.prosolo.web.lti.json.data.Service> services) throws RuntimeException; 
}