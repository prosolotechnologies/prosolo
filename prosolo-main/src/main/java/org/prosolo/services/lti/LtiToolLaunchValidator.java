package org.prosolo.services.lti;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;

import javax.servlet.http.HttpServletRequest;

public interface LtiToolLaunchValidator {
    void validateLaunch(LtiTool tool, String consumerKey, LtiVersion version, HttpServletRequest request) throws RuntimeException;
}