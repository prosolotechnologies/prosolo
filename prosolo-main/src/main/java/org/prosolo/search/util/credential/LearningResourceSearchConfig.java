package org.prosolo.search.util.credential;

public final class LearningResourceSearchConfig {

	private final boolean includeEnrolledResources;
	private final boolean includeResourcesWithViewPrivilege;
	private final boolean includeResourcesWithInstructPrivilege;
	private final boolean includeResourcesWithEditPrivilege;
	
	private LearningResourceSearchConfig(
			boolean includeEnrolledResources, 
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege) {
		this.includeEnrolledResources = includeEnrolledResources;
		this.includeResourcesWithViewPrivilege = includeResourcesWithViewPrivilege;
		this.includeResourcesWithInstructPrivilege = includeResourcesWithInstructPrivilege;
		this.includeResourcesWithEditPrivilege = includeResourcesWithEditPrivilege;
	}
	
	public static LearningResourceSearchConfig of(
			boolean includeEnrolledResources, 
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege) {
		return new LearningResourceSearchConfig(includeEnrolledResources, includeResourcesWithViewPrivilege, 
				includeResourcesWithInstructPrivilege, includeResourcesWithEditPrivilege);
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
