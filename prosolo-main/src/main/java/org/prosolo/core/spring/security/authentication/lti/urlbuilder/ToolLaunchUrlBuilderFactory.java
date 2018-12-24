package org.prosolo.core.spring.security.authentication.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.LtiTool;

public class ToolLaunchUrlBuilderFactory {

	public static ToolLaunchUrlBuilder getLaunchUrlBuilder(LtiTool ltiTool) {
		switch (ltiTool.getToolType()) {
			case Credential:
				return new CredentialUrlBuilder();
			case Competence:
				return new CompetenceUrlBuilder();
			case Global:
				return new GlobalUrlBuilder(ltiTool.getOrganization().getId());
		    default: 
			    return null;
		}
	}
}
