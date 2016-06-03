package org.prosolo.services.nodes;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;

public interface RoleManager extends AbstractManager {
	
	List<Role> getAllRoles();

	List<Long> getRoleIdsForName(String name);
	
	public Role createNewRole(String name, String description, boolean systemDefined, List<Long> capabilities);
	
	User assignRoleToUser(Role role, User user);

	User assignRoleToUser(Role role, long userId);
	
	List<User> getUsersWithRole(String role);
	
//	void assignRoleToUser(Role role, User user, OrganizationalUnit orgUnit, String position);

	Role getRoleByName(String roleUserTitle);

	Role getOrCreateNewRole(String name, String description,
			boolean systemDefined);
	
	boolean isUserAdmin(User user);

	User removeRoleFromUser(Role role, long id);

	User updateUserRoles(long userId, List<String> roles) throws ResourceCouldNotBeLoadedException;

	public Role updateRole(long id, String title, String description, List<Long> capabilities, List<Long> capabilitiesBeforeUpdate) throws ResourceCouldNotBeLoadedException;

	void deleteRole(long id) throws ResourceCouldNotBeLoadedException;

	boolean isRoleUsed(long roleId);

	List<Role> getUserRoles(String email);
	
	public Role saveRole(String name, String description, boolean systemDefined) throws DbConnectionException;
	
	public List<Capability> getRoleCapabilities(long roleId) throws DbConnectionException;
	
	public Map<Long, List<Long>> getUsersWithRoles(List<Role> roles) throws DbConnectionException;
	
	public List<String> getNamesOfRoleCapabilities(long roleId) throws DbConnectionException;
}
