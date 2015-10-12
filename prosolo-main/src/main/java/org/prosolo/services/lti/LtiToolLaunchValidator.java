package org.prosolo.services.lti;

import javax.servlet.http.HttpServletRequest;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;

public interface LtiToolLaunchValidator {
	public void validateLaunch(LtiTool tool, String consumerKey, LtiVersion version, HttpServletRequest request) throws RuntimeException;
}