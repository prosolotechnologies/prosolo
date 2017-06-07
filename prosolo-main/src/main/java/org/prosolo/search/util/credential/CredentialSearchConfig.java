package org.prosolo.search.util.credential;

import org.prosolo.common.domainmodel.credential.CredentialType;

public final class CredentialSearchConfig extends LearningResourceSearchConfig {

	private final CredentialType type;

	public CredentialSearchConfig(
			boolean includeEnrolledResources,
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege,
			CredentialType type) {
		super(includeEnrolledResources, includeResourcesWithViewPrivilege, includeResourcesWithInstructPrivilege,
				includeResourcesWithEditPrivilege);
		this.type = type;
	}
	
	public static CredentialSearchConfig forOriginal(boolean includeResourcesWithEditPrivilege) {
		return new CredentialSearchConfig(false, false, false, includeResourcesWithEditPrivilege, 
				CredentialType.Original);
	}
	
	public static CredentialSearchConfig forDelivery(
			boolean includeEnrolledResources, 
			boolean includeResourcesWithViewPrivilege, 
			boolean includeResourcesWithInstructPrivilege, 
			boolean includeResourcesWithEditPrivilege) {
		return new CredentialSearchConfig(includeEnrolledResources, includeResourcesWithViewPrivilege, 
				includeResourcesWithInstructPrivilege, includeResourcesWithEditPrivilege, CredentialType.Delivery);
	}

	public CredentialType getType() {
		return type;
	}
	
	
}
