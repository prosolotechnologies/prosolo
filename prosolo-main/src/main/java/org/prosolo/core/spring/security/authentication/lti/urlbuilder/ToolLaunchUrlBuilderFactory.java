package org.prosolo.core.spring.security.authentication.lti.urlbuilder;

import org.prosolo.services.lti.data.LTIToolData;

public class ToolLaunchUrlBuilderFactory {

	public static ToolLaunchUrlBuilder getLaunchUrlBuilder(LTIToolData ltiTool) {
		switch (ltiTool.getToolType()) {
			case Credential:
				return new CredentialUrlBuilder();
			case Competence:
				return new CompetenceUrlBuilder();
			case Global:
				return new GlobalUrlBuilder(ltiTool.getOrganizationId());
		    default: 
			    return null;
		}
	}
}
