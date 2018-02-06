package org.prosolo.web.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;

public abstract class ToolLaunchUrlBuilder {
	
	public String getLaunchUrl(LtiTool tool, long userId, long organizationId){
		return "/learn.xhtml?"+getUrlParameters(tool, userId);
	}

	protected abstract String getUrlParameters(LtiTool tool, long userId);
	
}
