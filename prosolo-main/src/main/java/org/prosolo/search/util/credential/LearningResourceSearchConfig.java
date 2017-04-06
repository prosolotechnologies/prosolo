package org.prosolo.search.util.credential;

public class LearningResourceSearchConfig {

	private final boolean includeEnrolledResources;
	private final boolean includeResourcesWithViewPrivilege;
	private final boolean includeResourcesWithInstructPrivilege;
	private final boolean includeResourcesWithEditPrivilege;
	
	protected LearningResourceSearchConfig(
			boolean includeEnrolledResources,
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege) {
		this.includeEnrolledResources = includeEnrolledResources;
		this.includeResourcesWithViewPrivilege = includeResourcesWithViewPrivilege;
		this.includeResourcesWithInstructPrivilege = includeResourcesWithInstructPrivilege;
		this.includeResourcesWithEditPrivilege = includeResourcesWithEditPrivilege;
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
	
}
