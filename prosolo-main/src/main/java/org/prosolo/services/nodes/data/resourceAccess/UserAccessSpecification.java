package org.prosolo.services.nodes.data.resourceAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

public abstract class UserAccessSpecification {

	private final Set<UserGroupPrivilege> privileges = new HashSet<>();
	private final boolean resourceVisibleToAll;
	private final boolean isUserResourceOwner;
	
	protected UserAccessSpecification(Collection<UserGroupPrivilege> privileges, boolean resourceVisibleToAll, 
			boolean isUserResourceOwner) {
		this.privileges.addAll(privileges);
		this.resourceVisibleToAll = resourceVisibleToAll;
		this.isUserResourceOwner = isUserResourceOwner;
	}
	
	/**
	 * This method allows visitor to visit user specification object.
	 * 
	 * @param visitor
	 */
	public abstract <T> T accept(UserAccessSpecificationVisitor<T> visitor);

//	/**
//	 * Returns whether user is allowed to learn resource if he has right privileges.
//	 * This method has nothing to do with checking if user has needed privileges, it only checks
//	 * additional conditions or preconditions that have to be met so access can be granted to user.
//	 * 
//	 * @return
//	 */
//	public abstract boolean isAllowedToLearn();
//	
//	/**
//	 * Returns whether user can be given instructor access to the resource if he has right privileges.
//	 * This method has nothing to do with checking if user has needed privileges, it only checks
//	 * additional conditions or preconditions that have to be met so access can be granted to user.
//	 * 
//	 * @return
//	 */
//	public abstract boolean isAllowedToInstruct();
//	
//	/**
//	 * Returns whether user is allowed to learn resource if he has right privileges.
//	 * So, this method has nothing with checking if user has needed privileges, but it checks
//	 * additional conditions or preconditions that have to be met for user to be able to learn.
//	 * 
//	 * @return
//	 */
//	public abstract boolean isAllowedToEdit();
	
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
	
}
