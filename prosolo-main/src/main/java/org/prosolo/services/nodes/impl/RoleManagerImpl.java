package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.RoleManager")
public class RoleManagerImpl extends AbstractManagerImpl implements RoleManager {

	private static final long serialVersionUID = -6380999989911355824L;

	private static Logger logger = Logger.getLogger(RoleManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;
	
	@Override
	public List<Long> getRoleIdsForName(String name){
		String query = 
				"SELECT r.id " +
				"FROM Role r " +
				"WHERE r.title = :name";
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
			.setParameter("name", name.toUpperCase())
			.list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}

		return new ArrayList<Long>();
	}
	
	@Override
	public List<Role> getAllRoles() {
		List<Role> result = getAllResources(Role.class);
		
		if (result != null && !result.isEmpty()) {
			return result;
		}

		return new ArrayList<Role>();
	}

	@Override
	public Role createNewRole(String name, String description, boolean systemDefined) {
		return resourceFactory.createNewRole(
				name,
				description,
				systemDefined);
	}
	
	@Override
	@Transactional (readOnly = false)
	public User assignRoleToUser(Role role, User user) {
		user = merge(user);

		if (user.getRoles().contains(role)) {
			return user;
		}
		
		user.addRole(role);
		return saveEntity(user);
	}
	
	@Override
	@Transactional (readOnly = false)
	public User assignRoleToUser(Role role, long userId) {
		try {
			User user = loadResource(User.class, userId);
			return assignRoleToUser(role, user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<User> getUsersWithRole(String role) {
		String query = 
			"SELECT user " +
			"FROM User user " +
			"LEFT JOIN user.roles role " +
			"WHERE role.title = :name";
		
		@SuppressWarnings("unchecked")
		List<User> users = persistence.currentManager().createQuery(query)
			.setParameter("name", role)
			.list();
		
		return users;
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<Role> getUserRoles(String email) {
		String query = 
				"SELECT role " +
						"FROM User user " +
						"LEFT JOIN user.email email " +
						"LEFT JOIN user.roles role "+
						"WHERE email.address = :email " ;
						
						
			 
		
		@SuppressWarnings("unchecked")
		List<Role> roles = persistence.currentManager().createQuery(query)
			.setParameter("email", email)
			.list();
		
		return roles;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Role getRoleByName(String roleName) {
		String query = 
				"SELECT r " +
				"FROM Role r " +
				"WHERE r.title = :name";
		
		return (Role) persistence.currentManager().createQuery(query)
			.setParameter("name", roleName)
			.uniqueResult();
	}
	
	@Override
	@Transactional (readOnly = false)
	public Role getOrCreateNewRole(String name, String description, boolean systemDefined) {
		Role role = getRoleByName(name);
		
		if (role == null) {
			role = createNewRole(name, description, systemDefined);
		}
		return role;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isUserAdmin(User user) {
		String query = 
			"SELECT COUNT(user) " +
			"FROM User user " +
			"LEFT JOIN user.roles role " +
			"WHERE user = :user " +
				"AND lower(role.title) = :roleName";
		
		Long count = (Long) persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setString("roleName", "Admin")
			.uniqueResult();
		
		return count > 0;
	}
	
	@Override
	@Transactional (readOnly = false)
	public User removeRoleFromUser(Role role, long id) {
		try {
			User user = loadResource(User.class, id);
			
			Iterator<Role> iterator = user.getRoles().iterator();
			
			while (iterator.hasNext()) {
				Role r = (Role) iterator.next();
				
				if (r.getId() == role.getId()) {
					iterator.remove();
					break;
				}
			}
			return saveEntity(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = false)
	public User updateUserRoles(long userId, List<String> roles) throws ResourceCouldNotBeLoadedException {
		User user = loadResource(User.class, userId);
		user.getRoles().clear();

		List<Role> allRoles = getAllRoles();
		
		Iterator<String> rolesIterator = roles.iterator();
		
		outer: while (rolesIterator.hasNext()){
			String roleId = String.valueOf(rolesIterator.next());

			for (Role role : allRoles) {
				if (role.getId() == Long.parseLong(roleId)) {
					user.addRole(role);
					continue outer;
				}
			}
		}
		return saveEntity(user);
	}
	
	@Override
	@Transactional (readOnly = false)
	public Role updateRole(long id, String title, String description) throws ResourceCouldNotBeLoadedException {
		Role role = loadResource(Role.class, id);
		role.setTitle(title);
		role.setDescription(description);
		
		return saveEntity(role);
	}
	
	@Override
	@Transactional (readOnly = false)
	public void deleteRole(long id) throws ResourceCouldNotBeLoadedException {
		Role role = loadResource(Role.class, id);
		
		delete(role);
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isRoleUsed(long roleId) {
		String query = 
			"SELECT COUNT(user) " +
			"FROM User user " +
			"LEFT JOIN user.roles role " +
			"WHERE role.id = :roleId";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
			.setLong("roleId", roleId)
			.uniqueResult();
		
		return result > 0;
	}
}
