package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.web.administration.data.RoleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("org.prosolo.services.nodes.RoleManager")
public class RoleManagerImpl extends AbstractManagerImpl implements RoleManager {

	private static final long serialVersionUID = -6380999989911355824L;

	private static Logger logger = Logger.getLogger(RoleManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;
	
	@Override
	@Transactional
	public Long getRoleIdByName(String name){
		String query = 
				"SELECT r.id " +
				"FROM Role r " +
				"WHERE lower(r.title) = :name";
		
		@SuppressWarnings("unchecked")
		Long result = (Long) persistence.currentManager().createQuery(query)
			.setParameter("name", name.toLowerCase())
			.uniqueResult();
		
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RoleData> getRolesByNames(String[] names) {
		 String query =
				 "SELECT role " +
				 "FROM Role role " +
				 "WHERE role.title IN :names";

		@SuppressWarnings("unchecked")
		List<Role> result = persistence.currentManager().createQuery(query)
				 .setParameterList("names", names)
				 .list();

		List<RoleData> roleDataList = new LinkedList<>();

		if (result != null && !result.isEmpty()) {
			for (Role role : result) {
				roleDataList.add(new RoleData(role));
			}
		}
		 return roleDataList;
	}

	@Override
	@Transactional (readOnly = true)
	public List<RoleData> getAllRoles() {
		List<Role> result = getAllResources(Role.class);

		List<RoleData> roleDataList = new LinkedList<>();
		
		if (result != null && !result.isEmpty()) {
			for (Role role : result) {
				roleDataList.add(new RoleData(role));
			}
		}

		return roleDataList;
	}

	@Override
	public Role createNewRole(String name, String description, boolean systemDefined) {
		return resourceFactory.createNewRole(
				name, description,
				systemDefined);
	}
	
	@Override
	@Transactional (readOnly = false)
	public User assignRoleToUser(Role role, long userId) {
		try {
			User user = loadResource(User.class, userId);

			if (user.getRoles().contains(role)) {
				return user;
			}

			user.addRole(role);
			return saveEntity(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return null;
	}

	@Override
	@Transactional (readOnly = false)
	public List<Role> getUserRoles(String email) {
		String query = 
				"SELECT role " +
				"FROM User user " +
				"INNER JOIN user.roles role "+
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
	public Role updateRole(long id, String title, String description) throws ResourceCouldNotBeLoadedException {
		Role role = loadResource(Role.class, id);
		role.setTitle(title);
		role.setDescription(description);
		role = saveEntity(role);
		
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
			throw new DbConnectionException("Error saving role");
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
			throw new DbConnectionException("Error loading capabilities");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<Long, List<Long>> getUsersWithRoles(List<Long> roleIds) throws DbConnectionException{
		try{
			String query = 
					"SELECT role.id, user.id " +
					"FROM User user " +
					"INNER JOIN user.roles role " +
					"WHERE role.id IN (:roleIds) " +
					"AND user.deleted = :deleted";
				
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager().createQuery(query).
					setParameterList("roleIds", roleIds)
					.setBoolean("deleted", false)
					.list();

			Map<Long, List<Long>> resultMap = new HashMap<>();

			if (result != null && !result.isEmpty()) {
				for (Object[] res : result) {
					Long roleId = (Long) res[0];
					Long userId = (Long) res[1];
					
					List<Long> users = resultMap.get(roleId);
					
					if (users == null) {
						users = new ArrayList<>();
					}
					users.add(userId);
					
					resultMap.put(roleId, users);
				}
			}
			return resultMap;
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error loading capabilities");
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
			throw new DbConnectionException("Error loading capabilities");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean hasAnyRole(long userId, List<String> roleNames) throws DbConnectionException {
		try {
			String query = 
				"SELECT role.id " +
				"FROM User user " +
				"LEFT JOIN user.roles role " +
				"WHERE user.id = :userId " +
				"AND role.title IN (:roleNames)";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setParameterList("roleNames", roleNames)
				.list();
			
			if(res == null || res.isEmpty()) {
				return false;
			}
			return true;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error checking user roles");
		}
	}

}
