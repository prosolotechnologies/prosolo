package org.prosolo.search.util.credential;

import org.prosolo.common.domainmodel.credential.LearningResourceType;

public class CompetenceSearchConfig extends LearningResourceSearchConfig {

	private final LearningResourceType resourceType;
	
	private CompetenceSearchConfig(
			boolean includeEnrolledResources,
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege,
			LearningResourceType resourceType) {
		super(includeEnrolledResources, includeResourcesWithViewPrivilege, includeResourcesWithInstructPrivilege,
				includeResourcesWithEditPrivilege);
		this.resourceType = resourceType;
	}
	
	public static CompetenceSearchConfig of(
			boolean includeEnrolledResources, 
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege,
			LearningResourceType resourceType) {
		return new CompetenceSearchConfig(includeEnrolledResources, includeResourcesWithViewPrivilege, 
				includeResourcesWithInstructPrivilege, includeResourcesWithEditPrivilege, resourceType);
	}

	public LearningResourceType getResourceType() {
		return resourceType;
	}
	
}
