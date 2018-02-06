package org.prosolo.web.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.spring.security.HomePageResolver;

/**
 * 
 * @author Nikola Milikic
 * @date 2018-01-24
 * @since 1.2.0
 */
public class GlobalUrlBuilder extends ToolLaunchUrlBuilder{

	@Override
	protected String getUrlParameters(LtiTool tool, long userId) {
		return null;
	}

	@Override
	public String getLaunchUrl(LtiTool tool, long userId, long organizationId) {
		// this method assumes user is already logged in as it reads his granted authorities from the Spring Security
		return new HomePageResolver().getHomeUrl(organizationId);
	}
}
