package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

public interface RoleManager extends AbstractManager {
	
	List<Role> getAllRoles();

	List<Long> getRoleIdsForName(String name);
	
	Role createNewRole(String name, String description, boolean systemDefined);
	
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

	Role updateRole(long id, String title, String description) throws ResourceCouldNotBeLoadedException;

	void deleteRole(long id) throws ResourceCouldNotBeLoadedException;

	boolean isRoleUsed(long roleId);

	List<Role> getUserRoles(String email);
}
