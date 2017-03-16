package org.prosolo.search.util.credential;

import org.prosolo.common.domainmodel.credential.LearningResourceType;

public final class LearningResourceSearchConfig {

	private final boolean includeEnrolledResources;
	private final boolean includeResourcesWithViewPrivilege;
	private final boolean includeResourcesWithInstructPrivilege;
	private final boolean includeResourcesWithEditPrivilege;
	private final LearningResourceType resourceType;
	
	private LearningResourceSearchConfig(
			boolean includeEnrolledResources,
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege,
			LearningResourceType resourceType) {
		this.includeEnrolledResources = includeEnrolledResources;
		this.includeResourcesWithViewPrivilege = includeResourcesWithViewPrivilege;
		this.includeResourcesWithInstructPrivilege = includeResourcesWithInstructPrivilege;
		this.includeResourcesWithEditPrivilege = includeResourcesWithEditPrivilege;
		this.resourceType = resourceType;
	}
	
	public static LearningResourceSearchConfig of(
			boolean includeEnrolledResources, 
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege,
			LearningResourceType resourceType) {
		return new LearningResourceSearchConfig(includeEnrolledResources, includeResourcesWithViewPrivilege, 
				includeResourcesWithInstructPrivilege, includeResourcesWithEditPrivilege, resourceType);
	}

	public boolean shouldIncludeEnrolledResources() {
		return includeEnrolledResources;
	}

	public boolean shouldIncludeResourcesWithViewPrivilege() {
		return includeResourcesWithViewPrivilege;
	}

	public boolean shouldIncludeResourcesWithInstructPrivilege() {
		return includeResourcesWithInstructPrivilege;
	}

	public boolean shouldIncludeResourcesWithEditPrivilege() {
		return includeResourcesWithEditPrivilege;
	}

	public LearningResourceType getResourceType() {
		return resourceType;
	}
	
}
