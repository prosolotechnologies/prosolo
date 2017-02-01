package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.services.nodes.data.UserGroupPrivilegeData;
import org.prosolo.services.nodes.factory.UserGroupPrivilegeDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.UserGroupManager")
public class UserGroupManagerImpl extends AbstractManagerImpl implements UserGroupManager {
	
	private static final long serialVersionUID = 8236179314516077700L;

	private static Logger logger = Logger.getLogger(UserGroupManagerImpl.class);
	
	@Inject private ResourceFactory resourceFactory;
	@Inject private EventFactory eventFactory;
	@Inject private UserGroupPrivilegeDataFactory groupPrivilegeFactory;
	 
	@Override
	@Transactional(readOnly = true)
	public List<UserGroup> getAllGroups() throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g";
			
			@SuppressWarnings("unchecked")
			List<UserGroup> result = persistence.currentManager().createQuery(query).list();
			
			return result == null ? new ArrayList<>() : result; 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving user groups");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserGroupData getGroup(long groupgId) throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g " +
				"WHERE g.id = :groupId ";
			
			UserGroup result = (UserGroup) persistence.currentManager().createQuery(query)
				.setLong("groupId", groupgId)
				.uniqueResult();
			
			return new UserGroupData(result); 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving group");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserGroupData> searchGroups(String searchTerm, int limit, int page) 
			throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g " +
				"WHERE g.name LIKE :term " +
				"ORDER BY g.name ASC";
			
			String term = searchTerm == null ? "" : searchTerm;
			@SuppressWarnings("unchecked")
			List<UserGroup> result = persistence.currentManager()
					.createQuery(query)
					.setString("term", "%" + term)
					.setFirstResult(page)
					.setMaxResults(limit)
					.list();
			
			if(result == null) {
				return new ArrayList<>();
			}
			List<UserGroupData> groups = new ArrayList<>();
			for(UserGroup group : result) {
				groups.add(new UserGroupData(group.getId(), group.getName(), group.getUsers().size()));
			}
			return groups;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving user groups");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countGroups(String searchTerm) throws DbConnectionException {
		try {
			String query = 
				"SELECT COUNT(g.id) " +
				"FROM UserGroup g " +
				"WHERE g.name LIKE :term";
			
			String term = searchTerm == null ? "" : searchTerm;
			Long result = (Long) persistence.currentManager()
					.createQuery(query)
					.setString("term", "%" + term)
					.uniqueResult();
			
			return result == null ? 0 : result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving number of groups");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserGroup saveNewGroup(String name, boolean isDefault, long userId, 
			LearningContextData context) throws DbConnectionException {
		try {
			UserGroup group = resourceFactory.saveNewGroup(name, isDefault);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Create, userId, group, null, page, lContext,
					service, null);
			return group;
		} catch(DbConnectionException dbce) {
			throw dbce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public UserGroup updateGroupName(long groupId, String newName, long userId, 
			LearningContextData context) throws DbConnectionException {
		try {
			UserGroup group = resourceFactory.updateGroupName(groupId, newName);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Edit, userId, group, null, page, lContext,
					service, null);
			return group;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public UserGroup updateJoinUrl(long groupId, boolean joinUrlActive, String joinUrlPassword, long userId,
			LearningContextData context) {
		try {
			UserGroup group = resourceFactory.updateGroupJoinUrl(groupId, joinUrlActive, joinUrlPassword);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Edit, userId, group, null, page, lContext,
					service, null);
			return group;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving user group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteUserGroup(long id, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, id);
			delete(group);
			
			//generate delete event
			UserGroup deletedGroup = new UserGroup();
			deletedGroup.setId(id);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Delete, userId, deletedGroup, null, page, lContext,
					service, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while deleting user group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void addUserToTheGroup(long groupId, long userId) throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			User user = (User) persistence.currentManager().load(User.class, userId);
			UserGroupUser ugUser = new UserGroupUser();
			ugUser.setGroup(group);
			ugUser.setUser(user);
			saveEntity(ugUser);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while adding the user to the group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void removeUserFromTheGroup(long groupId, long userId) throws DbConnectionException {
		try {
			Optional<UserGroupUser> groupUser = getUserGroupUser(groupId, userId);
			if(groupUser.isPresent()) {
				delete(groupUser.get());
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while removing user from the group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void addUsersToTheGroup(long groupId, List<Long> userIds) throws DbConnectionException {
		try {
			for(Long user : userIds) {
				addUserToTheGroup(groupId, user);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while adding users to the group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void removeUsersFromTheGroup(long groupId, List<Long> userIds) throws DbConnectionException {
		try {
			for(Long user : userIds) {
				removeUserFromTheGroup(groupId, user);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while removing users from the group");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void updateGroupUsers(long groupId, List<Long> usersToAdd, List<Long> usersToRemove)
			throws DbConnectionException {
		try {
			if (usersToAdd != null && !usersToAdd.isEmpty()) {
				addUsersToTheGroup(groupId, usersToAdd);
			}
			
			if (usersToRemove != null && !usersToRemove.isEmpty()) {
				removeUsersFromTheGroup(groupId, usersToRemove);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while updating group users");
		}
		
	}

	@Override
	@Transactional(readOnly = false)
	public void addUserToGroups(long userId, List<Long> groupIds) throws DbConnectionException {
		try {
			for(Long group : groupIds) {
				addUserToTheGroup(group, userId);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while adding user to groups");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void removeUserFromGroups(long userId, List<Long> groupIds) throws DbConnectionException {
		try {
			for(Long group : groupIds) {
				removeUserFromTheGroup(group, userId);
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while removing user from groups");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateUserParticipationInGroups(long userId, List<Long> groupsToRemoveUserFrom, 
			List<Long> groupsToAddUserTo) throws DbConnectionException {
		try {
			addUserToGroups(userId, groupsToAddUserTo);
			removeUserFromGroups(userId, groupsToRemoveUserFrom);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while updating user groups");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
    public long getNumberOfUsersInAGroup(long groupId) throws DbConnectionException {
    	try {
    		String query = "SELECT COUNT(groupUser.id) FROM UserGroupUser groupUser " +
				   	   "WHERE groupUser.group.id = :groupId";
			Long count = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.uniqueResult();
			return count == null ? 0 : count;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving number of users in a group");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public boolean isUserInGroup(long groupId, long userId) throws DbConnectionException {
    	try {
    		String query = "SELECT groupUser.id FROM UserGroupUser groupUser " +
				   	   "WHERE groupUser.group.id = :groupId " +
				   	   "AND groupUser.user.id = :userId";
			Long id = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.setLong("userId", userId)
					.uniqueResult();
			return id == null ? false : true;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while checking if user belongs to a group");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CredentialUserGroup> getCredentialUserGroups(long groupId) throws DbConnectionException {
    	try {
    		String query = "SELECT credGroup FROM CredentialUserGroup credGroup " +
				   	   "WHERE credGroup.userGroup.id = :groupId";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.list();
			return credGroups == null ? new ArrayList<>() : credGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CredentialUserGroup> getAllCredentialUserGroups(long credId) 
    		throws DbConnectionException {
    	return getAllCredentialUserGroups(credId, persistence.currentManager());
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CredentialUserGroup> getAllCredentialUserGroups(long credId, Session session) 
    		throws DbConnectionException {
    	try {
    		String query = "SELECT credGroup FROM CredentialUserGroup credGroup " +
				   	   "WHERE credGroup.credential.id = :credId";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = session
					.createQuery(query)
					.setLong("credId", credId)
					.list();
			return credGroups == null ? new ArrayList<>() : credGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CompetenceUserGroup> getAllCompetenceUserGroups(long compId) 
    		throws DbConnectionException {
    	return getAllCompetenceUserGroups(compId, persistence.currentManager());
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CompetenceUserGroup> getAllCompetenceUserGroups(long compId, Session session) 
    		throws DbConnectionException {
    	try {
    		String query = "SELECT compGroup FROM CompetenceUserGroup compGroup " +
				   	   "WHERE compGroup.competence.id = :compId";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = session
					.createQuery(query)
					.setLong("compId", compId)
					.list();
			return compGroups == null ? new ArrayList<>() : compGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CompetenceUserGroup> getCompetenceUserGroups(long groupId) throws DbConnectionException {
    	try {
    		String query = "SELECT compGroup FROM CompetenceUserGroup compGroup " +
				   	   "WHERE compGroup.userGroup.id = :groupId";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.list();
			return compGroups == null ? new ArrayList<>() : compGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCredentialVisibilityGroups(long credId) 
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
    		String query = "SELECT credGroup FROM CredentialUserGroup credGroup " +
    					   "INNER JOIN fetch credGroup.userGroup userGroup " +
    					   "WHERE credGroup.credential.id = :credId " +
    					   "AND userGroup.defaultGroup = :defaultGroup ";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setBoolean("defaultGroup", false)
					.list();
			if(credGroups != null) {
				for(CredentialUserGroup group : credGroups) {
					UserGroupPrivilegeData priv = groupPrivilegeFactory.getUserGroupPrivilegeData(
							group.getPrivilege());
					members.add(new ResourceVisibilityMember(group.getId(), group.getUserGroup().getId(),
							group.getUserGroup().getName(), group.getUserGroup().getUsers().size(), 
							priv, true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCredentialVisibilityUsers(long credId) 
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			String query = "SELECT distinct credGroup FROM CredentialUserGroup credGroup " +
					   "INNER JOIN fetch credGroup.userGroup userGroup " +
					   "LEFT JOIN fetch userGroup.users users " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND userGroup.defaultGroup = :defaultGroup ";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> defaultGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setBoolean("defaultGroup", true)
					.list();
			if(defaultGroups != null) {
				for(CredentialUserGroup group : defaultGroups) {
					List<UserGroupUser> users = group.getUserGroup().getUsers();
					for(UserGroupUser u : users) {
						UserGroupPrivilegeData priv = groupPrivilegeFactory.getUserGroupPrivilegeData(
								group.getPrivilege());
						members.add(new ResourceVisibilityMember(u.getId(), u.getUser(), 
								priv, true));
					}
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential users");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public void saveCredentialUsersAndGroups(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users) 
    				throws DbConnectionException {
    	try {
    		saveCredentialUsers(credId, users);
    		saveCredentialGroups(credId, groups);
    	} catch(DbConnectionException dce) {
    		throw dce;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential users and groups");
    	}
    }
	
	@Transactional(readOnly = false)
    private void saveCredentialUsers(long credId, List<ResourceVisibilityMember> users) 
    		throws DbConnectionException {
    	try {
    		if(users == null) {
    			return;
    		}
    		for(ResourceVisibilityMember user : users) {
    			UserGroupUser userGroupUser = null;
    			CredentialUserGroup credGroup = null;
    			switch(user.getStatus()) {
    				case CREATED:
    					saveUserToDefaultCredentialGroup(user.getUserId(), credId, 
    							groupPrivilegeFactory.getUserGroupPrivilege(user.getPrivilege()));
    					break;
    				case CHANGED:
    					userGroupUser = (UserGroupUser) persistence
    							.currentManager().load(UserGroupUser.class, user.getId());
    					credGroup = getOrCreateDefaultCredentialUserGroup(credId, 
    							groupPrivilegeFactory.getUserGroupPrivilege(user.getPrivilege()));
    					userGroupUser.setGroup(credGroup.getUserGroup());
    					break;
    				case REMOVED:
    					userGroupUser = (UserGroupUser) persistence
							.currentManager().load(UserGroupUser.class, user.getId());
    					delete(userGroupUser);
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential users");
    	}
    }
	
	private void saveUserToDefaultCredentialGroup(long userId, long credId, UserGroupPrivilege privilege) {
		UserGroupUser userGroupUser = new UserGroupUser();
		User u = (User) persistence.currentManager().load(User.class, userId);
		userGroupUser.setUser(u);
		CredentialUserGroup credGroup = getOrCreateDefaultCredentialUserGroup(credId, 
				privilege);
		userGroupUser.setGroup(credGroup.getUserGroup());
		saveEntity(userGroupUser);
	}
	
	@Override
	@Transactional(readOnly = true)
    public boolean isUserInADefaultCredentialGroup(long userId, long credId) 
    		throws DbConnectionException {
		try {
			String query = "SELECT user.id FROM CredentialUserGroup credGroup " +
					   "INNER JOIN credGroup.userGroup userGroup " +
					   "INNER JOIN userGroup.users user " +
					   		"WITH user.id = :userId " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND userGroup.defaultGroup = :defaultGroup ";
			@SuppressWarnings("unchecked")
			List<Long> users = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setBoolean("defaultGroup", true)
					.list();
			return users != null && !users.isEmpty();
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while checking if user is in a default credential group");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public void addUserToADefaultCredentialGroupIfNotAlreadyMember(long userId, long credId,
    		UserGroupPrivilege privilege) throws DbConnectionException {
		try {
			if(!isUserInADefaultCredentialGroup(userId, credId)) {
				saveUserToDefaultCredentialGroup(userId, credId, privilege);
			}
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while adding user to a default credential group");
    	}
    }
	
	@Transactional(readOnly = false)
    private void saveCredentialGroups(long credId, List<ResourceVisibilityMember> groups) 
    		throws DbConnectionException {
    	try {
    		if(groups == null) {
    			return;
    		}
    		for(ResourceVisibilityMember group : groups) {
    			CredentialUserGroup credGroup = null;
    			switch(group.getStatus()) {
    				case CREATED:
    					credGroup = new CredentialUserGroup();
    					Credential1 cred = (Credential1) persistence.currentManager().load(
    							Credential1.class, credId);
    					credGroup.setCredential(cred);
    					UserGroup userGroup = (UserGroup) persistence.currentManager().load(
    							UserGroup.class, group.getGroupId());
    					credGroup.setUserGroup(userGroup);
    					credGroup.setPrivilege(groupPrivilegeFactory.getUserGroupPrivilege(
    							group.getPrivilege()));
    					saveEntity(credGroup);
    					break;
    				case CHANGED:
    					credGroup = (CredentialUserGroup) persistence
    							.currentManager().load(CredentialUserGroup.class, group.getId());
    					credGroup.setPrivilege(groupPrivilegeFactory.getUserGroupPrivilege(
    							group.getPrivilege()));
    					break;
    				case REMOVED:
    					credGroup = (CredentialUserGroup) persistence
							.currentManager().load(CredentialUserGroup.class, group.getId());
    					delete(credGroup);
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential groups");
    	}
    }
	
	private CredentialUserGroup getOrCreateDefaultCredentialUserGroup(long credId, UserGroupPrivilege priv) {
		Optional<CredentialUserGroup> credGroupOptional = getCredentialDefaultGroup(credId, priv);
		CredentialUserGroup credGroup = null;
		if(credGroupOptional.isPresent()) {
			credGroup = credGroupOptional.get();
		} else {
			UserGroup userGroup = new UserGroup();
			userGroup.setDefaultGroup(true);
			saveEntity(userGroup);
			credGroup = new CredentialUserGroup();
			credGroup.setUserGroup(userGroup);
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credId);
			credGroup.setCredential(cred);
			credGroup.setPrivilege(priv);
			saveEntity(credGroup);
		}
		return credGroup;
	}
	
	private Optional<CredentialUserGroup> getCredentialDefaultGroup(long credId, UserGroupPrivilege privilege) {
		String query = "SELECT credGroup FROM CredentialUserGroup credGroup " +
					   "INNER JOIN credGroup.userGroup userGroup " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND credGroup.privilege = :priv " +
					   "AND userGroup.defaultGroup = :default";
		
		CredentialUserGroup credGroup = (CredentialUserGroup) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setParameter("priv", privilege)
				.setBoolean("default", true)
				.setMaxResults(1)
				.uniqueResult();
		
		return credGroup != null ? Optional.of(credGroup) : Optional.empty();
	}
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCompetenceVisibilityGroups(long compId) 
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
    		String query = "SELECT compGroup FROM CompetenceUserGroup compGroup " +
    					   "INNER JOIN fetch compGroup.userGroup userGroup " +
    					   "WHERE compGroup.competence.id = :compId " +
    					   "AND userGroup.defaultGroup = :defaultGroup ";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setBoolean("defaultGroup", false)
					.list();
			if(compGroups != null) {
				for(CompetenceUserGroup group : compGroups) {
					UserGroupPrivilegeData priv = groupPrivilegeFactory.getUserGroupPrivilegeData(
							group.getPrivilege());
					members.add(new ResourceVisibilityMember(group.getId(), group.getUserGroup().getId(),
							group.getUserGroup().getName(), group.getUserGroup().getUsers().size(), 
							priv, true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId) 
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			String query = "SELECT distinct compGroup FROM CompetenceUserGroup compGroup " +
					   "INNER JOIN fetch compGroup.userGroup userGroup " +
					   "LEFT JOIN fetch userGroup.users users " +
					   "WHERE compGroup.competence.id = :compId " +
					   "AND userGroup.defaultGroup = :defaultGroup ";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> defaultGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setBoolean("defaultGroup", true)
					.list();
			if(defaultGroups != null) {
				for(CompetenceUserGroup group : defaultGroups) {
					List<UserGroupUser> users = group.getUserGroup().getUsers();
					for(UserGroupUser u : users) {
						UserGroupPrivilegeData priv = groupPrivilegeFactory.getUserGroupPrivilegeData(
								group.getPrivilege());
						members.add(new ResourceVisibilityMember(u.getId(), u.getUser(), 
								priv, true));
					}
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving competence users");
    	}
    }
	
	private Optional<UserGroupUser> getUserGroupUser(long groupId, long userId) {
		String query = "SELECT groupUser FROM UserGroupUser groupUser " +
				   	   "WHERE groupUser.user.id = :userId " +
				   	   "AND groupUser.group.id = :groupId";
		UserGroupUser groupUser = (UserGroupUser) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setLong("userId", userId)
				.uniqueResult();
		return groupUser != null ? Optional.of(groupUser) : Optional.empty();
	}
	
	@Override
	@Transactional(readOnly = false)
    public void saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users) throws DbConnectionException {
    	try {
    		saveCompetenceUsers(compId, users);
    		saveCompetenceGroups(compId, groups);
    	} catch(DbConnectionException dce) {
    		throw dce;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving competence users and groups");
    	}
    }
	
	@Transactional(readOnly = false)
    private void saveCompetenceUsers(long compId, List<ResourceVisibilityMember> users) 
    		throws DbConnectionException {
    	try {
    		if(users == null) {
    			return;
    		}
    		for(ResourceVisibilityMember user : users) {
    			UserGroupUser userGroupUser = null;
    			CompetenceUserGroup compGroup = null;
    			switch(user.getStatus()) {
    				case CREATED:
    					userGroupUser = new UserGroupUser();
    					User u = (User) persistence.currentManager().load(User.class, user.getUserId());
    					userGroupUser.setUser(u);
    					compGroup = getOrCreateDefaultCompetenceUserGroup(compId, 
    							groupPrivilegeFactory.getUserGroupPrivilege(user.getPrivilege()));
    					userGroupUser.setGroup(compGroup.getUserGroup());
    					saveEntity(userGroupUser);
    					break;
    				case CHANGED:
    					userGroupUser = (UserGroupUser) persistence
    							.currentManager().load(UserGroupUser.class, user.getId());
    					compGroup = getOrCreateDefaultCompetenceUserGroup(compId, 
    							groupPrivilegeFactory.getUserGroupPrivilege(user.getPrivilege()));
    					userGroupUser.setGroup(compGroup.getUserGroup());
    					break;
    				case REMOVED:
    					userGroupUser = (UserGroupUser) persistence
							.currentManager().load(UserGroupUser.class, user.getId());
    					delete(userGroupUser);
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving competence users");
    	}
    }
	
	@Transactional(readOnly = false)
    private void saveCompetenceGroups(long compId, List<ResourceVisibilityMember> groups) 
    		throws DbConnectionException {
    	try {
    		if(groups == null) {
    			return;
    		}
    		for(ResourceVisibilityMember group : groups) {
    			CompetenceUserGroup compGroup = null;
    			switch(group.getStatus()) {
    				case CREATED:
    					compGroup = new CompetenceUserGroup();
    					Competence1 comp = (Competence1) persistence.currentManager().load(
    							Competence1.class, compId);
    					compGroup.setCompetence(comp);
    					UserGroup userGroup = (UserGroup) persistence.currentManager().load(
    							UserGroup.class, group.getGroupId());
    					compGroup.setUserGroup(userGroup);
    					compGroup.setPrivilege(groupPrivilegeFactory.getUserGroupPrivilege(
    							group.getPrivilege()));
    					saveEntity(compGroup);
    					break;
    				case CHANGED:
    					compGroup = (CompetenceUserGroup) persistence
    							.currentManager().load(CompetenceUserGroup.class, group.getId());
    					compGroup.setPrivilege(groupPrivilegeFactory.getUserGroupPrivilege(
    							group.getPrivilege()));
    					break;
    				case REMOVED:
    					compGroup = (CompetenceUserGroup) persistence
							.currentManager().load(CompetenceUserGroup.class, group.getId());
    					delete(compGroup);
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving competence groups");
    	}
    }
	
	private CompetenceUserGroup getOrCreateDefaultCompetenceUserGroup(long compId, 
			UserGroupPrivilege priv) {
		Optional<CompetenceUserGroup> compGroupOptional = getCompetenceDefaultGroup(compId, priv);
		CompetenceUserGroup compGroup = null;
		if(compGroupOptional.isPresent()) {
			compGroup = compGroupOptional.get();
		} else {
			UserGroup userGroup = new UserGroup();
			userGroup.setDefaultGroup(true);
			saveEntity(userGroup);
			compGroup = new CompetenceUserGroup();
			compGroup.setUserGroup(userGroup);
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
					compId);
			compGroup.setCompetence(comp);
			compGroup.setPrivilege(priv);
			saveEntity(compGroup);
		}
		return compGroup;
	}
	
	private Optional<CompetenceUserGroup> getCompetenceDefaultGroup(long compId, 
			UserGroupPrivilege privilege) {
		String query = "SELECT compGroup FROM CompetenceUserGroup compGroup " +
					   "INNER JOIN compGroup.userGroup userGroup " +
					   "WHERE compGroup.competence.id = :compId " +
					   "AND compGroup.privilege = :priv " +
					   "AND userGroup.defaultGroup = :default";
		
		CompetenceUserGroup compGroup = (CompetenceUserGroup) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setParameter("priv", privilege)
				.setBoolean("default", true)
				.setMaxResults(1)
				.uniqueResult();
		
		return compGroup != null ? Optional.of(compGroup) : Optional.empty();
	}
	
}
