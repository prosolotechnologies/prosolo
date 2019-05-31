package org.prosolo.services.lti;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.services.lti.data.LTIToolData;

import javax.servlet.http.HttpServletRequest;

public interface LtiToolLaunchValidator {
    void validateLaunch(LTIToolData tool, String consumerKey, LtiVersion version, HttpServletRequest request) throws RuntimeException;
}