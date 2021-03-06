package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.administration.data.RoleData;

import java.util.List;
import java.util.Map;

public interface RoleManager extends AbstractManager {

    List<RoleData> getAllRoles();

    List<RoleData> getRolesByNames(String[] names);

    Long getRoleIdByName(String name);

    Role createNewRole(String name, String description, boolean systemDefined);

    User assignRoleToUser(Role role, long userId);

    Role getRoleByName(String roleUserTitle);

    Role updateRole(long id, String title, String description) throws ResourceCouldNotBeLoadedException;

    void deleteRole(long id) throws ResourceCouldNotBeLoadedException;

    List<Role> getUserRoles(String email);

    Role saveRole(String name, String description, boolean systemDefined) throws DbConnectionException;

    List<Capability> getRoleCapabilities(long roleId) throws DbConnectionException;

    Map<Long, List<Long>> getUsersWithRoles(List<Long> roleIds) throws DbConnectionException;

    List<String> getNamesOfRoleCapabilities(long roleId) throws DbConnectionException;

    boolean hasAnyRole(long userId, List<String> roleNames) throws DbConnectionException;

}
