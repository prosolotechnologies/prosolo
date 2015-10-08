package org.prosolo.services.lti;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.lti.filter.Filter;


public interface LtiToolManager {

	public LtiTool saveLtiTool(LtiTool tool);
	public LtiTool updateLtiTool(LtiTool tool) throws ResourceCouldNotBeLoadedException;
	public LtiTool changeEnabled (long toolId, boolean enabled) throws ResourceCouldNotBeLoadedException;
	public LtiTool deleteLtiTool(long toolId) throws ResourceCouldNotBeLoadedException;
	public LtiTool getToolDetails(long toolId);
	public List<LtiTool> searchTools(long userId, String name, Map<String,Object> parameters, Filter filter);
	public LtiTool getLtiToolForLaunch(HttpServletRequest request, String key, LtiVersion ltiVersion, long toolId) throws RuntimeException;
	public List<LtiTool> getToolsForToolProxy(long toolSetId);
}