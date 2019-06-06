package org.prosolo.services.lti.impl;

import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.services.lti.LtiToolLaunchValidator;
import org.prosolo.services.lti.data.LTIConsumerData;
import org.prosolo.services.lti.data.LTIToolData;
import org.prosolo.services.lti.exceptions.LtiToolAccessDeniedException;
import org.prosolo.services.lti.exceptions.LtiToolDeletedException;
import org.prosolo.services.lti.exceptions.LtiToolDisabledException;
import org.prosolo.services.oauth.OauthService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Service("org.prosolo.services.lti.LtiToolLaunchValidator")
public class LtiToolLaunchValidatorImpl implements LtiToolLaunchValidator {
	
	@Inject private OauthService oauthService;
	
	@Override
	public void validateLaunch(LTIToolData tool, String consumerKey, LtiVersion version, HttpServletRequest request) throws RuntimeException {
		if (tool == null){

			throw new LtiToolAccessDeniedException();
		}
		if (!tool.isEnabled()){
			throw new LtiToolDisabledException();
		}
		if (tool.isDeleted()) {
			throw new LtiToolDeletedException();
		}
		LTIConsumerData consumer = tool.getConsumer();
		String key;
		String secret;
		if (LtiVersion.V1.equals(version)){
			key = consumer.getKeyLtiOne();
			secret = consumer.getSecretLtiOne();
		} else {
			key = consumer.getKeyLtiTwo();
			secret = consumer.getSecretLtiTwo();
		}
		if (consumer == null || !key.equals(consumerKey) ){
			throw new LtiToolAccessDeniedException();
		}
		try {
			oauthService.validatePostRequest(request, tool.getLaunchUrl(), key, secret);
		} catch(Exception e) {
			throw new LtiToolAccessDeniedException();
		}
	}
}
