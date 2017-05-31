package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
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
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ObjectStatus;
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
	@Inject private CredentialManager credManager;
	 
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
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCredentialVisibilityGroups(long credId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			List<CredentialUserGroup> credGroups = getCredentialUserGroups(credId, false, privilege,
					persistence.currentManager());
			if (credGroups != null) {
				for(CredentialUserGroup group : credGroups) {
					members.add(new ResourceVisibilityMember(group.getId(), group.getUserGroup().getId(),
							group.getUserGroup().getName(), group.getUserGroup().getUsers().size(), 
							group.getPrivilege(), true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential groups");
    	}
    }
	
	/**
	 * 
	 * @param credId
	 * @param returnDefaultGroups
	 * @param privilege - if only groups with specific privilege should be returned pass that privilege here,
	 * otherwise pass null
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
    private List<CredentialUserGroup> getCredentialUserGroups (long credId, boolean returnDefaultGroups, 
    		UserGroupPrivilege privilege, Session session) throws DbConnectionException {
		try {
    		StringBuilder query = new StringBuilder (
    					   "SELECT credGroup FROM CredentialUserGroup credGroup " +
    					   "INNER JOIN fetch credGroup.userGroup userGroup " +
    					   "WHERE credGroup.credential.id = :credId ");
    		if (!returnDefaultGroups) {
    			query.append("AND userGroup.defaultGroup = :defaultGroup ");
    		}
    		if (privilege != null) {
    			query.append("AND credGroup.privilege = :priv ");
    		}
			Query q = session
						.createQuery(query.toString())
						.setLong("credId", credId);
			
			if (!returnDefaultGroups) {
				q.setBoolean("defaultGroup", false);
			}
			if (privilege != null) {
				q.setParameter("priv", privilege);
			}
			
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = q.list();
			
			return credGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving credential groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCredentialVisibilityUsers(long credId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			StringBuilder query = new StringBuilder("SELECT distinct credGroup FROM CredentialUserGroup credGroup " +
					   "INNER JOIN fetch credGroup.userGroup userGroup " +
					   "LEFT JOIN fetch userGroup.users users " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND userGroup.defaultGroup = :defaultGroup ");

			if (privilege != null) {
				query.append("AND credGroup.privilege = :priv ");
			}

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setBoolean("defaultGroup", true);

			if (privilege != null) {
				q.setString("priv", privilege.name());
			}

			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> defaultGroups = q.list();

			if(defaultGroups != null) {
				for(CredentialUserGroup group : defaultGroups) {
					List<UserGroupUser> users = group.getUserGroup().getUsers();
					for(UserGroupUser u : users) {
						members.add(new ResourceVisibilityMember(u.getId(), u.getUser(),
								group.getPrivilege(), true));
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
    public Result<Void> saveCredentialUsersAndGroups(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, long actorId, LearningContextData lcd) throws DbConnectionException {
    	try {
    		if(groups == null || users == null) {
    			throw new NullPointerException("Invalid argument values");
    		}
    		List<EventData> events = new ArrayList<>();
    		events.addAll(saveCredentialUsers(credId, users, actorId, lcd).getEvents());
    		events.addAll(saveCredentialGroups(credId, groups, actorId, lcd).getEvents());
    		Credential1 cred = new Credential1();
    		cred.setId(credId);
    		
    		//see if visibility is actually changed
    		boolean visibilityChanged = false;
    		for (ResourceVisibilityMember g : groups) {
    			if (g.getStatus() != ObjectStatus.UP_TO_DATE) {
    				visibilityChanged = true;
    				break;
    			}
    		}
    		if (!visibilityChanged) {
    			for (ResourceVisibilityMember u : users) {
    				if (u.getStatus() != ObjectStatus.UP_TO_DATE) {
    					visibilityChanged = true;
    					break;
    				}
    			}
    		}
    		
    		if (visibilityChanged) {
    			events.add(eventFactory.generateEventData(
        				EventType.RESOURCE_VISIBILITY_CHANGE, actorId, cred, null, lcd, null));
    		}
    		
    		Result<Void> res = new Result<>();
    		res.setEvents(events);
    		return res;
    	} catch(DbConnectionException dce) {
    		throw dce;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential users and groups");
    	}
    }
	
	@Transactional(readOnly = false)
    private Result<Void> saveCredentialUsers(long credId, List<ResourceVisibilityMember> users, long actorId, 
    		LearningContextData context) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		if (users == null) {
    			return res;
    		}
    		if (!users.isEmpty()) {
    			//store reference to default groups for learn and edit privilege to be reused for different users
    			CredentialUserGroup learnCredGroup = null;
    			CredentialUserGroup editCredGroup = null;
    			Map<Long, EventData> userGroupsChangedEvents = new HashMap<>(); 
	    		for (ResourceVisibilityMember user : users) {
	    			UserGroupUser userGroupUser = null;
	    			switch (user.getStatus()) {
	    				case CREATED:
	    					Result<CredentialUserGroup> credUserGroupRes = getOrCreateDefaultCredentialUserGroup(
	    							credId, user.getPrivilege(), actorId, context);
	    					res.addEvents(credUserGroupRes.getEvents());
	    					CredentialUserGroup gr = credUserGroupRes.getResult();
	    					saveNewUserToCredentialGroup(user.getUserId(), gr);
	    					if (user.getPrivilege() == UserGroupPrivilege.Edit) {
	    						editCredGroup = gr;
	    					} else {
	    						learnCredGroup = gr;
	    					}
	    					generateUserGroupChangeEventIfNotGenerated(gr.getUserGroup().getId(), actorId, 
	    							context, userGroupsChangedEvents);
	    					break;
	    				case REMOVED:
	    					userGroupUser = (UserGroupUser) persistence
								.currentManager().load(UserGroupUser.class, user.getId());
	    					delete(userGroupUser);
	    					generateUserGroupChangeEventIfNotGenerated(userGroupUser.getGroup().getId(), actorId, 
	    							context, userGroupsChangedEvents);
	    					break;
	    				case UP_TO_DATE:
	    					break;
	    			}
	    		}
	    		
	    		/*
	    		 * user group change event should not be generated for newly created groups. Since
	    		 * these groups are default groups user group added to resource event actually means
	    		 * that group is newly created so user group change event is removed from map for such 
	    		 * user groups.
	    		 */
	    		res.getEvents().stream()
	    			.filter(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE)
	    			.map(ev -> ev.getObject().getId())
	    			.forEach(id -> userGroupsChangedEvents.remove(id));
	    		
	    		//generate all user group change events
	    		userGroupsChangedEvents.forEach((key, event) -> res.addEvent(event));
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential users");
    	}
    }
	
	private void generateUserGroupChangeEventIfNotGenerated(long userGroupId, long actorId, 
			LearningContextData context, Map<Long, EventData> userGroupsChangedEvents) {
		UserGroup ug = new UserGroup();
		ug.setId(userGroupId);
		//if event for this user group is not already generated, generate it and put it in the map
		if(userGroupsChangedEvents.get(userGroupId) == null) {
			userGroupsChangedEvents.put(userGroupId, 
					eventFactory.generateEventData(EventType.USER_GROUP_CHANGE, actorId, ug, 
							null, context, null));
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Result<Void> saveUserToDefaultCredentialGroupAndGetEvents(long userId, long credId, 
			UserGroupPrivilege privilege, long actorId, LearningContextData context) throws DbConnectionException {
		try {
			Result<CredentialUserGroup> credGroup = getOrCreateDefaultCredentialUserGroup(credId, privilege, actorId, 
					context);
			saveNewUserToCredentialGroup(userId, credGroup.getResult());
			Result<Void> res = new Result<>();
			res.setEvents(credGroup.getEvents());
			boolean groupJustCreated = res.getEvents().stream()
				.anyMatch(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE);
			//if user group is not just created, generate add user to group event
			if (!groupJustCreated) {
				User object = new User();
				object.setId(userId);
				UserGroup target = new UserGroup();
				target.setId(credGroup.getResult().getUserGroup().getId());
				res.addEvent(eventFactory.generateEventData(EventType.ADD_USER_TO_GROUP, actorId, object, target, 
						context, null));
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding privilege to a user for credential");
		}
	}
	
	private void saveNewUserToCredentialGroup(long userId, CredentialUserGroup credGroup) {
		if(credGroup == null) {
			throw new NullPointerException();
		}
		UserGroupUser userGroupUser = new UserGroupUser();
		User u = (User) persistence.currentManager().load(User.class, userId);
		userGroupUser.setUser(u);
		userGroupUser.setGroup(credGroup.getUserGroup());
		saveEntity(userGroupUser);
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> removeUserFromCredentialDefaultGroupAndGetEvents(long credId, long userId, 
			UserGroupPrivilege privilege, long actorId, LearningContextData context) throws DbConnectionException {
		try {
			String query = "SELECT ugu FROM CredentialUserGroup cug " +
					       "INNER JOIN cug.userGroup ug " +
					       "INNER JOIN ug.users ugu " +
					       		"WITH ugu.user.id = :userId " +
					       "WHERE cug.credential.id = :credId " +
					       "AND cug.privilege = :priv " +
					       "AND ug.defaultGroup = :isDefault";
			UserGroupUser ugu = (UserGroupUser) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setString("priv", privilege.name())
					.setBoolean("isDefault", true)
					.uniqueResult();
			
			return removeUserFromGroupAndGetEvents(ugu, actorId, context);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while removing user privilege from credential");
		}
	}
	
	@Transactional(readOnly = false)
	private Result<Void> removeUserFromGroupAndGetEvents(UserGroupUser userGroupUser, long actorId, 
			LearningContextData context) {
		Result<Void> res = new Result<>();
		
		User object = new User();
		object.setId(userGroupUser.getUser().getId());
		UserGroup target = new UserGroup();
		target.setId(userGroupUser.getGroup().getId());
		res.addEvent(eventFactory.generateEventData(
				EventType.REMOVE_USER_FROM_GROUP, actorId, object, target, context, null));
		delete(userGroupUser);
		return res;
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
	
	@Transactional(readOnly = false)
    private Result<Void> saveCredentialGroups(long credId, List<ResourceVisibilityMember> groups, long userId, 
    		LearningContextData lcd) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		
    		if (groups == null) {
    			return res;
    		}
    		
    		for (ResourceVisibilityMember group : groups) {
    			switch(group.getStatus()) {
    				case CREATED:
    					res.addEvents(
    							createNewCredentialUserGroup(group.getGroupId(), false, credId, 
    									group.getPrivilege(), userId, lcd)
    										.getEvents());
    					break;
    				case REMOVED:
    					res.addEvents(
    							removeCredentialUserGroup(credId, group.getId(), group.getGroupId(), userId, lcd)
    								.getEvents());
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving credential groups");
    	}
    }
	
	private Result<Void> removeCredentialUserGroup(long credId, long credUserGroupId, long userGroupId, long userId, 
			LearningContextData lcd) {
		CredentialUserGroup credGroup = (CredentialUserGroup) persistence
				.currentManager().load(CredentialUserGroup.class, credUserGroupId);
		
		Result<Void> res = new Result<>();
		UserGroup userGroup = new UserGroup();
		userGroup.setId(userGroupId);
		Credential1 cred = new Credential1();
		cred.setId(credId);
		Map<String, String> params = new HashMap<>();
		params.put("credentialUserGroupId", credGroup.getId() + "");
		params.put("privilege", credGroup.getPrivilege().name());
		res.addEvent(eventFactory.generateEventData(EventType.USER_GROUP_REMOVED_FROM_RESOURCE, userId, userGroup, 
				cred, lcd, null));

		delete(credGroup);
		
		return res;
	}
	
//	private Result<Void> changeCredentialUserGroupPrivilege(long credId, long credUserGroupId, UserGroupPrivilege priv,
//			long userId, LearningContextData lcd) {
//		CredentialUserGroup credGroup = (CredentialUserGroup) persistence
//				.currentManager().load(CredentialUserGroup.class, credUserGroupId);
//		credGroup.setPrivilege(priv);
//
//		Result<Void> res = new Result<>();
//		CredentialUserGroup cug = new CredentialUserGroup();
//		cug.setId(credUserGroupId);
//		Credential1 cred = new Credential1();
//		cred.setId(credId);
//		cug.setCredential(cred);
//		Map<String, String> params = new HashMap<>();
//		params.put("privilege", priv.toString());
//		res.addEvent(eventFactory.generateEventData(EventType.RESOURCE_USER_GROUP_PRIVILEGE_CHANGE, userId, cug, null,
//				lcd, params));
//
//		return res;
//	}
	
	private Result<CredentialUserGroup> getOrCreateDefaultCredentialUserGroup(long credId, UserGroupPrivilege priv,
			long userId, LearningContextData lcd) {
		Optional<CredentialUserGroup> credGroupOptional = getCredentialDefaultGroup(credId, priv);
		Result<CredentialUserGroup> res = new Result<>();
		if(credGroupOptional.isPresent()) {
			res.setResult(credGroupOptional.get());
		} else {
			res = createNewCredentialUserGroup(0, true, credId, priv, userId, lcd);
		}
		return res;
	}
	
	/**
	 * Creates and persists new CredentialUserGroup instance based on existing or new user group.
	 * 
	 * @param userGroupId - 0 if new user group should be created
	 * @param isDefault - true if it is a default group
	 * @param credId
	 * @param priv
	 * @param userId
	 * @param lcd
	 * @return
	 */
	private Result<CredentialUserGroup> createNewCredentialUserGroup(long userGroupId, boolean isDefault, long credId, 
			UserGroupPrivilege priv, long userId, LearningContextData lcd) {
		UserGroup userGroup = null;
		if(userGroupId > 0) {
			userGroup = (UserGroup) persistence.currentManager().load(UserGroup.class, userGroupId);
		} else {
			userGroup = new UserGroup();
			userGroup.setDefaultGroup(isDefault);
			saveEntity(userGroup);
		}
		CredentialUserGroup credGroup = new CredentialUserGroup();
		credGroup.setUserGroup(userGroup);
		Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
		credGroup.setCredential(cred);
		credGroup.setPrivilege(priv);
		saveEntity(credGroup);
		
		UserGroup ug = new UserGroup();
		ug.setId(userGroup.getId());
		Credential1 credential = new Credential1();
		credential.setId(credId);
		Map<String, String> params = new HashMap<>();
		params.put("default", isDefault + "");
		params.put("credentialUserGroupId", credGroup.getId() + "");
		params.put("privilege", priv.name());
		EventData ev = eventFactory.generateEventData(EventType.USER_GROUP_ADDED_TO_RESOURCE, userId, ug, credential, 
				lcd, params);
		
		Result<CredentialUserGroup> res = new Result<>();
		res.setResult(credGroup);
		res.addEvent(ev);
		
		return res;
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
					members.add(new ResourceVisibilityMember(group.getId(), group.getUserGroup().getId(),
							group.getUserGroup().getName(), group.getUserGroup().getUsers().size(), 
							group.getPrivilege(), true));
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
						members.add(new ResourceVisibilityMember(u.getId(), u.getUser(),
								group.getPrivilege(), true));
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
    		if (users == null) {
    			return;
    		}
    		for (ResourceVisibilityMember user : users) {
    			UserGroupUser userGroupUser = null;
    			CompetenceUserGroup compGroup = null;
    			switch (user.getStatus()) {
    				case CREATED:
    					userGroupUser = new UserGroupUser();
    					User u = (User) persistence.currentManager().load(User.class, user.getUserId());
    					userGroupUser.setUser(u);
    					compGroup = getOrCreateDefaultCompetenceUserGroup(compId, user.getPrivilege());
    					userGroupUser.setGroup(compGroup.getUserGroup());
    					saveEntity(userGroupUser);
    					break;
    				case CHANGED:
    					userGroupUser = (UserGroupUser) persistence
    							.currentManager().load(UserGroupUser.class, user.getId());
    					compGroup = getOrCreateDefaultCompetenceUserGroup(compId, user.getPrivilege());
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
    					compGroup.setPrivilege(group.getPrivilege());
    					saveEntity(compGroup);
    					break;
    				case CHANGED:
    					compGroup = (CompetenceUserGroup) persistence
    							.currentManager().load(CompetenceUserGroup.class, group.getId());
    					compGroup.setPrivilege(group.getPrivilege());
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
		Optional<CompetenceUserGroup> compGroupOptional = getCompetenceDefaultGroup(compId, priv, false);
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
			UserGroupPrivilege privilege, boolean returnIfInherited) {
		StringBuilder query = new StringBuilder("SELECT compGroup FROM CompetenceUserGroup compGroup " +
					   "INNER JOIN compGroup.userGroup userGroup " +
					   "WHERE compGroup.competence.id = :compId " +
					   "AND compGroup.privilege = :priv " +
					   "AND userGroup.defaultGroup = :default ");
		
		if (!returnIfInherited) {
			query.append("AND compGroup.inherited = :inherited");
		}
		
		Query q = persistence.currentManager()
				.createQuery(query.toString())
				.setLong("compId", compId)
				.setParameter("priv", privilege)
				.setBoolean("default", true)
				.setMaxResults(1);
		
		if(!returnIfInherited) {
			q.setBoolean("inherited", false);
		}
		
		CompetenceUserGroup compGroup = (CompetenceUserGroup) q.uniqueResult();
		
		return compGroup != null ? Optional.of(compGroup) : Optional.empty();
	}
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> removeUserGroupPrivilegePropagatedFromCredentialAndGetEvents(long credId, long userGroupId, 
    		Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		res.addEvents(removeUserGroupPrivilegeFromCompetencesAndGetEvents(credId, userGroupId, session)
    				.getEvents());
    		res.addEvents(removeUserGroupPrivilegeFromDeliveriesAndGetEvents(credId, userGroupId, session)
    				.getEvents());
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Transactional(readOnly = false)
    private Result<Void> removeUserGroupPrivilegeFromCompetencesAndGetEvents(long credId, long userGroupId, 
    		Session session) throws DbConnectionException {
    	try {
    		String query = "DELETE FROM CompetenceUserGroup gr " +
    					   "WHERE gr.userGroup.id = :userGroupId " +
    				       "AND gr.inherited = :inherited " +
    				       "AND gr.inheritedFrom.id = :credId";
    		
    		int affected = session.createQuery(query)
    			   .setLong("userGroupId", userGroupId)
    			   .setBoolean("inherited", true)
    			   .setLong("credId", credId)
    			   .executeUpdate();
    		
    		Result<Void> res = new Result<>();
    		if (affected > 0) {
	    		//retrieve compIds so appropriate events can be generated for each competence
	    		List<Long> compIds = credManager.getIdsOfAllCompetencesInACredential(credId, session);
	    		for (long compId : compIds) {
	    			/*
	        		 * generate only resource visibility change event, user group removed from resource event is not needed 
	        		 * because it is inherited group
	        		 */
	        		Competence1 comp = new Competence1();
	        		comp.setId(compId);
	        		res.addEvent(eventFactory.generateEventData(
	        				EventType.RESOURCE_VISIBILITY_CHANGE, 0, comp, null, null, null));
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Transactional(readOnly = false)
    private Result<Void> removeUserGroupPrivilegeFromDeliveriesAndGetEvents(long credId, long userGroupId, 
    		Session session) throws DbConnectionException {
    	try {
    		String query = "DELETE gr FROM credential_user_group gr " +
    					   "INNER JOIN credential1 c on gr.credential = c.id " +
    					   "WHERE gr.privilege = :editPriv " +
    					   "AND gr.user_group = :userGroupId " +
    				       "AND c.delivery_of = :credId";
    		
    		int affected = session.createSQLQuery(query)
    			   .setString("editPriv", UserGroupPrivilege.Edit.name())
    			   .setLong("userGroupId", userGroupId)
    			   .setLong("credId", credId)
    			   .executeUpdate();
    		
    		logger.info("credential delivery user groups deleted: " + affected);
    		
    		Result<Void> res = new Result<>();
    		if (affected > 0) {
	    		//retrieve deliveries ids so appropriate events can be generated for each delivery
	    		List<Long> deliveries = credManager.getIdsOfAllCredentialDeliveries(credId, session);
	    		for (long delId : deliveries) {
	    			/*
	        		 * generate only resource visibility change event, user group removed from resource event is not needed 
	        		 * because it is inherited group
	        		 */
	        		Credential1 del = new Credential1();
	        		del.setId(delId);
	        		res.addEvent(eventFactory.generateEventData(
	        				EventType.RESOURCE_VISIBILITY_CHANGE, 0, del, null, null, null));
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(long compId, long credId, 
    		Session session) throws DbConnectionException {
    	try {
    		String query = "DELETE FROM CompetenceUserGroup gr " +
    				       "WHERE gr.competence.id = :compId " +
    				       "AND gr.inherited = :inherited " +
    				       "AND gr.inheritedFrom.id = :credId " +
					       "AND gr.privilege = :priv";
    		
    		session.createQuery(query)
    			   .setLong("compId", compId)
    			   .setBoolean("inherited", true)
    			   .setLong("credId", credId)
				   .setString("priv", UserGroupPrivilege.Edit.name())
    			   .executeUpdate();
    		
    		/*
    		 * generate only resource visibility change event, user group removed from resource event is not needed 
    		 * because it is inherited group
    		 */
    		Competence1 comp = new Competence1();
    		comp.setId(compId);
    		Result<Void> res = new Result<>();
    		res.addEvent(eventFactory.generateEventData(
    				EventType.RESOURCE_VISIBILITY_CHANGE, 0, comp, null, null, null));
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupPrivilegeFromCredentialAndGetEvents(long credUserGroupId, 
    		Session session) throws DbConnectionException {
		Result<Void> res = new Result<>();
		res.addEvents(propagateUserGroupPrivilegeFromCredentialToAllCompetencesAndGetEvents(
				credUserGroupId, session).getEvents());
		res.addEvents(propagateUserGroupPrivilegeFromCredentialToAllDeliveriesAndGetEvents(
				credUserGroupId, session).getEvents());
		return res;
    }
	
	@Transactional(readOnly = false)
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToAllCompetencesAndGetEvents(long credUserGroupId, 
    		Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		CredentialUserGroup credUserGroup = (CredentialUserGroup) session.load(CredentialUserGroup.class, credUserGroupId);
    		List<Long> compIds = credManager.getIdsOfAllCompetencesInACredential(credUserGroup.getCredential().getId(), session);
    		for (long compId : compIds) {
    			res.addEvents(propagateUserGroupPrivilegeFromCredential(credUserGroup, compId, session).getEvents());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	/**
	 * Propagates privilege to all credential deliveries but only if it is edit privilege.
	 * 
	 * @param credUserGroupId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = false)
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToAllDeliveriesAndGetEvents(long credUserGroupId, 
    		Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		CredentialUserGroup credUserGroup = (CredentialUserGroup) session.load(CredentialUserGroup.class, 
    				credUserGroupId);
    		//only edit privileges are propagated to deliveries
    		if(credUserGroup.getPrivilege() == UserGroupPrivilege.Edit) {
	    		List<Long> deliveries = credManager.getIdsOfAllCredentialDeliveries(
	    				credUserGroup.getCredential().getId(), session);
	    		for (long deliveryId : deliveries) {
	    			res.addEvents(propagateUserGroupPrivilegeFromCredentialToDelivery(credUserGroup, deliveryId, session)
	    					.getEvents());
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupPrivilegesFromCredentialToCompetenceAndGetEvents(long credId, long compId, 
    		Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		//we should propagate all groups, event default
    		List<CredentialUserGroup> credGroups = getCredentialUserGroups(credId, true,
					UserGroupPrivilege.Edit, session);
    		for (CredentialUserGroup credGroup : credGroups) {
    			res.addEvents(propagateUserGroupPrivilegeFromCredential(credGroup, compId, session).getEvents());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Transactional(readOnly = false)
    private Result<Void> propagateUserGroupPrivilegeFromCredential(CredentialUserGroup credUserGroup, long compId, 
    		Session session) throws DbConnectionException {
    	try {
    		CompetenceUserGroup cug = new CompetenceUserGroup();
    		Competence1 comp = (Competence1) session.load(Competence1.class, compId);
    		cug.setCompetence(comp);
    		cug.setUserGroup(credUserGroup.getUserGroup());
    		cug.setPrivilege(credUserGroup.getPrivilege());
    		cug.setInherited(true);
    		cug.setInheritedFrom(credUserGroup.getCredential());
    		saveEntity(cug, session);
    		
    		/*
    		 * we generate only resource visibility change event and not user group added to resource event because 
    		 * it is inherited group and for now we don't need to generate this event.
    		 */
    		Competence1 competence =  new Competence1();
    		competence.setId(comp.getId());
    		Result<Void> res = new Result<>();
    		//actor is zero because this method is always called from a background process and it is not triggered by user
    		res.addEvent(eventFactory.generateEventData(
    				EventType.RESOURCE_VISIBILITY_CHANGE, 0, competence, null, null, null));
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(long credId, 
    		long deliveryId, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		//we should propagate all groups, event default
    		List<CredentialUserGroup> credGroups = getCredentialUserGroups(credId, true, UserGroupPrivilege.Edit, 
    				session);
    		for (CredentialUserGroup credGroup : credGroups) {
    			res.addEvents(propagateUserGroupPrivilegeFromCredentialToDelivery(credGroup, deliveryId, session)
    					.getEvents());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Transactional(readOnly = false)
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToDelivery(CredentialUserGroup credUserGroup, 
    		long deliveryId, Session session) throws DbConnectionException {
    	try {
    		CredentialUserGroup cug = new CredentialUserGroup();
    		Credential1 del = (Credential1) session.load(Credential1.class, deliveryId);
    		cug.setCredential(del);
    		cug.setUserGroup(credUserGroup.getUserGroup());
    		cug.setPrivilege(credUserGroup.getPrivilege());
    		saveEntity(cug, session);
    		
    		/*
    		 * we generate only resource visibility change event and not user group added to resource event because 
    		 * it is inherited group and for now we don't need to generate this event.
    		 */
    		Credential1 delivery =  new Credential1();
    		delivery.setId(deliveryId);
    		Result<Void> res = new Result<>();
    		//actor is zero because this method is always called from a background process and it is not triggered by user
    		res.addEvent(eventFactory.generateEventData(
    				EventType.RESOURCE_VISIBILITY_CHANGE, 0, delivery, null, null, null));
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<Long> getIdsOfUserGroupsAddedToCredential(long credId, boolean returnDefaultGroups, 
    		UserGroupPrivilege privilege, Session session) throws DbConnectionException {
		try {
    		StringBuilder query = new StringBuilder (
    					   "SELECT ug.id FROM CredentialUserGroup credGroup " +
    					   "INNER JOIN credGroup.userGroup ug " +
    					   "WHERE credGroup.credential.id = :credId ");
    		if (!returnDefaultGroups) {
    			query.append("AND ug.defaultGroup = :defaultGroup ");
    		}
    		if (privilege != null) {
    			query.append("AND credGroup.privilege = :priv ");
    		}
			Query q = session
						.createQuery(query.toString())
						.setLong("credId", credId);
			
			if (!returnDefaultGroups) {
				q.setBoolean("defaultGroup", false);
			}
			if (privilege != null) {
				q.setParameter("priv", privilege);
			}
			
			@SuppressWarnings("unchecked")
			List<Long> groups = q.list();
			
			return groups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error while retrieving user groups");
    	}
    }
	
}
