package org.prosolo.web.lti.urlbuilder;

import org.prosolo.common.domainmodel.lti.ResourceType;

public class ToolLaunchUrlBuilderFactory {

	public static ToolLaunchUrlBuilder getLaunchUrlBuilder(ResourceType resourceType){
		switch(resourceType){
			case Credential:
				return new CredentialUrlBuilder();
			case Competence:
				return new CompetenceUrlBuilder();
		    default: 
			    return null;
		}
	}
}
