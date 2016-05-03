package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CapabilityManager;
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
	@Inject private CapabilityManager capabilityManager;
	
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
	public Role createNewRole(String name, String description, boolean systemDefined, List<Long> capabilities) {
		return resourceFactory.createNewRole(
				name, description,
				systemDefined, capabilities);
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
				"LEFT JOIN user.roles role "+
				"WHERE user.email = :email " ;
							
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
			role = createNewRole(name, description, systemDefined, null);
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
	public Role updateRole(long id, String title, String description, List<Long> capabilities, List<Long> capabilitiesBeforeUpdate) throws ResourceCouldNotBeLoadedException {
		Role role = loadResource(Role.class, id);
		role.setTitle(title);
		role.setDescription(description);
		role = saveEntity(role);
		
		for(long capId:capabilities){
			Capability cap = capabilityManager.getCapabilityWithRoles(capId);
			
			if(cap.getRoles() == null || !cap.getRoles().contains(role)){
				cap.getRoles().add(role);
			}
			saveEntity(cap);
		}
		for(long capId:capabilitiesBeforeUpdate){
			if(!capabilities.contains(capId)){
				Capability cap = capabilityManager.getCapabilityWithRoles(capId);
				cap.getRoles().remove(role);
			}
		}
		return role;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void deleteRole(long id) throws ResourceCouldNotBeLoadedException {
		Role role = loadResource(Role.class, id);
		
		List<Capability> capabilities = getRoleCapabilities(id);
		for(Capability cap:capabilities){
			cap.getRoles().remove(role);
		}
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
	
	@Override
	@Transactional
	public Role saveRole(String name, String description, boolean systemDefined) throws DbConnectionException{
		try{
			Role role = new Role();
			role.setTitle(name);
			role.setDescription(description);
			role.setDateCreated(new Date());
			role.setSystem(systemDefined);
			return saveEntity(role);
		}catch(Exception e){
			throw new DbConnectionException("Error while saving role");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Capability> getRoleCapabilities(long roleId) throws DbConnectionException{
		try{
			String query = 
					"SELECT caps " +
					"FROM Role role " +
					"INNER JOIN role.capabilities caps " +
					"WHERE role.id = :roleId";
				
				return persistence.currentManager().createQuery(query)
					.setLong("roleId", roleId)
					.list();
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capabilities");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<Long, List<Long>> getUsersWithRoles(List<Role> roles) throws DbConnectionException{
		try{
			String query = 
					"SELECT role.id, user.id " +
					"FROM User user " +
					"INNER JOIN user.roles role " +
					"WHERE role IN (:roles)";
				
			List<Object[]> result = persistence.currentManager().createQuery(query).
					setParameterList("roles", roles).
					list();
			Map<Long, List<Long>> resultMap = new HashMap<Long, List<Long>>();
			if (result != null && !result.isEmpty()) {
				for (Object[] res : result) {
					Long roleId = (Long) res[0];
					Long userId = (Long) res[1];
					
					List<Long> users = resultMap.get(roleId);
					
					if (users == null) {
						users = new ArrayList<Long>();
					}
					users.add(userId);
					
					resultMap.put(roleId, users);
				}
			}
			return resultMap;
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capabilities");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<String> getNamesOfRoleCapabilities(long roleId) throws DbConnectionException{
		try{
			String query = 
					"SELECT cap.name " +
					"FROM Capability cap " +
					"INNER JOIN cap.roles role " +
					"WHERE role.id = :roleId";
				
				return persistence.currentManager().createQuery(query)
					.setLong("roleId", roleId)
					.list();
				
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error while loading capabilities");
		}
	}
}
