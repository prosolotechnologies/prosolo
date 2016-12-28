package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.UserGroupData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.UserGroupManager")
public class UserGroupManagerImpl extends AbstractManagerImpl implements UserGroupManager {
	
	private static final long serialVersionUID = 8236179314516077700L;

	private static Logger logger = Logger.getLogger(UserGroupManagerImpl.class);
	
	@Inject private ResourceFactory resourceFactory;
	@Inject private EventFactory eventFactory;
	 
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
	public List<UserGroupData> searchGroups(String searchTerm, int limit, int page) 
			throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g " +
				"WHERE g.name LIKE :term";
			
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
			throw new DbConnectionException("Error while deleting user group");
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
			addUsersToTheGroup(groupId, usersToAdd);
			removeUsersFromTheGroup(groupId, usersToRemove);
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
	
	private Optional<UserGroupUser> getUserGroupUser(long groupId, long userId) {
		String query = "SELECT groupUser FROM UserGroupUser groupUser " +
				   	   "WHERE groupUser.user.id = :userId " +
				   	   "AND groupUser.group.id = :groupId";
		UserGroupUser groupUser = (UserGroupUser) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setLong("userId", userId)
				.uniqueResult();
		return groupUser == null ? Optional.of(groupUser) : Optional.empty();
	}
	
	
}
