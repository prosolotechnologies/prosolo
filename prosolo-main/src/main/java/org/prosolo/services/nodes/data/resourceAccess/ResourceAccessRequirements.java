package org.prosolo.services.nodes.data.resourceAccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

/**
 * Use addPrivilege method to add all privileges that are enough for a given context to be able to access the resource.
 * If user has any of the added privileges, he can access the resource.
 * 
 * @author stefanvuckovic
 *
 */
public class ResourceAccessRequirements {

	private final Set<UserGroupPrivilege> privileges = new HashSet<>();
	private final AccessMode accessMode;
	
	private ResourceAccessRequirements(AccessMode accessMode) {
		this.accessMode = accessMode;
	}
	
	/**
	 * 
	 * @param accessMode
	 * @return
	 * @throws NullPointerException - when accessMode is null
	 */
	public static ResourceAccessRequirements of(AccessMode accessMode) {
		if(accessMode == null) {
			throw new NullPointerException();
		}
		return new ResourceAccessRequirements(accessMode);
	}
	
	/**
	 * Adds privilege.
	 * 
	 * @param privilege
	 * @return
	 * @throws NullPointerException - when privilege is null
	 */
	public ResourceAccessRequirements addPrivilege(UserGroupPrivilege privilege) {
		if(privilege == null) {
			throw new NullPointerException();
		}
		privileges.add(privilege);
		return this;
	}

	/**
	 * Returns new copy of internal collection of privileges, so adding/removing elements from
	 * returned collection will not influence internal collection
	 * 
	 * @return
	 */
	public List<UserGroupPrivilege> getPrivileges() {
		return new ArrayList<>(privileges);
	}

	public AccessMode getAccessMode() {
		return accessMode;
	}
	
}
