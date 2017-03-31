package org.prosolo.services.nodes.data.resourceAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

public class UserAccessSpecification {

	private final Set<UserGroupPrivilege> privileges = new HashSet<>();
	private final boolean resourceVisibleToAll;
	private final boolean isUserResourceOwner;
	private final boolean resourcePublished;
	private final LearningResourceType resourceType;
	
	private UserAccessSpecification(Collection<UserGroupPrivilege> privileges, boolean resourceVisibleToAll, 
			boolean isUserResourceOwner, boolean resourcePublished, LearningResourceType resourceType) {
		this.privileges.addAll(privileges);
		this.resourceVisibleToAll = resourceVisibleToAll;
		this.isUserResourceOwner = isUserResourceOwner;
		this.resourcePublished = resourcePublished;
		this.resourceType = resourceType;
	}
	
	/**
	 * Returns {@link UserAccessSpecification} object based on provided data.
	 * 
	 * 
	 * @param privileges
	 * @param resourceVisibleToAll
	 * @param isUserResourceOwner
	 * @param resourcePublished
	 * @param resourceType
	 * @return
	 * @throws IllegalArgumentException - when {@code privileges} collection is either null or empty, 
	 * or when {@code resourceType} is null
	 */
	public static UserAccessSpecification of(Collection<UserGroupPrivilege> privileges, boolean resourceVisibleToAll, 
			boolean isUserResourceOwner, boolean resourcePublished, LearningResourceType resourceType) {
		if(privileges == null || privileges.isEmpty() || resourceType == null) {
			throw new IllegalArgumentException();
		}
		return new UserAccessSpecification(privileges, resourceVisibleToAll, isUserResourceOwner, resourcePublished, 
				resourceType);
	}

	/**
	 * Returns copy of internal collection.
	 * 
	 * @return
	 */
	public ArrayList<UserGroupPrivilege> getPrivileges() {
		return new ArrayList<>(privileges);
	}

	public boolean isResourceVisibleToAll() {
		return resourceVisibleToAll;
	}

	public boolean isUserResourceOwner() {
		return isUserResourceOwner;
	}

	public boolean isResourcePublished() {
		return resourcePublished;
	}

	public LearningResourceType getResourceType() {
		return resourceType;
	}
	
}
