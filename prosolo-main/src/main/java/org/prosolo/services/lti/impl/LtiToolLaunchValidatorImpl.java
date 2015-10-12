package org.prosolo.services.lti.impl;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.services.lti.LtiToolLaunchValidator;
import org.prosolo.services.oauth.OauthService;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.lti.LtiToolLaunchValidator")
public class LtiToolLaunchValidatorImpl implements LtiToolLaunchValidator {
	
	@Inject private OauthService oauthService;
	
	@Override
	public void validateLaunch(LtiTool tool, String consumerKey, LtiVersion version, HttpServletRequest request) throws RuntimeException{
		if (tool == null){
			throw new RuntimeException("You don't have access to this tool");
		}
		if(!tool.isEnabled()){
			throw new RuntimeException("Tool is disabled");
		}
		if(tool.isDeleted()){
			throw new RuntimeException("Tool is deleted");
		}
		LtiConsumer consumer = tool.getToolSet().getConsumer();
		String key = null;
		String secret = null;
		if(LtiVersion.V1.equals(version)){
			key = consumer.getKeyLtiOne();
			secret = consumer.getSecretLtiOne();
		}else{
			key = consumer.getKeyLtiTwo();
			secret = consumer.getSecretLtiTwo();
		}
		if(consumer == null || !key.equals(consumerKey) ){
			throw new RuntimeException("You are not allowed to access this tool");
		}
		try{
			oauthService.validatePostRequest(request, tool.getLaunchUrl(), key, secret);
		}catch(Exception e){
			throw new RuntimeException("You are not allowed to access this tool");
		}
	}
}
