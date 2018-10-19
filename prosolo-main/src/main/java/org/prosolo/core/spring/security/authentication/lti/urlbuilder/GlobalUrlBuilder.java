package org.prosolo.core.spring.security.authentication.lti.urlbuilder;

import org.prosolo.core.spring.security.HomePageResolver;

/**
 * 
 * @author Nikola Milikic
 * @date 2018-01-24
 * @since 1.2.0
 */
public class GlobalUrlBuilder implements ToolLaunchUrlBuilder {

	private final long organizationId;

	public GlobalUrlBuilder(long organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public String getLaunchUrl() {
		// this method assumes user is already logged in as it reads his granted authorities from the Spring Security
		return new HomePageResolver().getHomeUrl(organizationId);
	}
}
