package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.events.EventType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

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
					.setFirstResult(page * limit)
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
    public List<CompetenceUserGroup> getCompetenceUserGroups(long groupId)
			throws DbConnectionException {
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
	    			CredentialUserGroup credGroup;
	    			switch (user.getStatus()) {
						case CREATED:
							if (user.getPrivilege() == UserGroupPrivilege.Edit) {
								if (editCredGroup == null) {
									Result<CredentialUserGroup> credUserGroupRes = getOrCreateDefaultCredentialUserGroup(
											credId, user.getPrivilege(), actorId, context);
									res.addEvents(credUserGroupRes.getEvents());
									editCredGroup = credUserGroupRes.getResult();
								}
								credGroup = editCredGroup;
							} else {
								if (learnCredGroup == null) {
									Result<CredentialUserGroup> credUserGroupRes = getOrCreateDefaultCredentialUserGroup(
											credId, user.getPrivilege(), actorId, context);
									res.addEvents(credUserGroupRes.getEvents());
									learnCredGroup = credUserGroupRes.getResult();
								}
								credGroup = learnCredGroup;
							}
							saveNewUserToCredentialGroup(user.getUserId(), credGroup);
							generateUserGroupChangeEventIfNotGenerated(credGroup.getUserGroup().getId(), actorId,
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

	@Override
	@Transactional(readOnly = true)
	public Result<Void> removeUserFromDefaultCredentialGroupAndGetEvents(long userId, long credId,
																	 UserGroupPrivilege privilege, long actorId,
																	 LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			UserGroupUser ugu = getUserFromDefaultCredentialUserGroup(userId, credId, privilege);
			if (ugu != null) {
				result.addEvents(removeUserFromGroupAndGetEvents(ugu, actorId, context).getEvents());
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing privilege for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Result<Void> saveUserToDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																	 UserGroupPrivilege privilege, long actorId,
																	 LearningContextData context) throws DbConnectionException {
		try {
			Result<CompetenceUserGroup> compGroup = getOrCreateDefaultCompetenceUserGroup(compId, privilege, actorId,
					context);
			saveNewUserToCompetenceGroup(userId, compGroup.getResult());
			Result<Void> res = new Result<>();
			res.addEvents(compGroup.getEvents());
			boolean groupJustCreated = res.getEvents().stream()
					.anyMatch(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE);
			//if user group is not just created, generate add user to group event
			if (!groupJustCreated) {
				User object = new User();
				object.setId(userId);
				UserGroup target = new UserGroup();
				target.setId(compGroup.getResult().getUserGroup().getId());
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

	@Override
	@Transactional(readOnly = true)
	public Result<Void> removeUserFromDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																		 UserGroupPrivilege privilege, long actorId,
																		 LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			UserGroupUser ugu = getUserFromDefaultCompetenceUserGroup(userId, compId, privilege);
			if (ugu != null) {
				result.addEvents(removeUserFromGroupAndGetEvents(ugu, actorId, context).getEvents());
			}

			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing privilege for credential");
		}
	}

	private UserGroupUser getUserFromDefaultCompetenceUserGroup(long userId, long compId, UserGroupPrivilege priv) {
		String query = "SELECT ugu FROM CompetenceUserGroup cug " +
				"INNER JOIN cug.userGroup ug " +
				"WITH ug.defaultGroup = :boolTrue " +
				"INNER JOIN ug.users ugu " +
				"WITH ugu.user.id = :userId " +
				"WHERE cug.competence.id = :compId " +
				"AND cug.privilege = :priv " +
				"AND cug.inherited = :boolFalse";

		return (UserGroupUser) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setBoolean("boolTrue", true)
				.setLong("userId", userId)
				.setString("priv", priv.name())
				.setBoolean("boolFalse", false)
				.uniqueResult();
	}

	private UserGroupUser getUserFromDefaultCredentialUserGroup(long userId, long credId, UserGroupPrivilege priv) {
		String query = "SELECT ugu FROM CredentialUserGroup cug " +
				"INNER JOIN cug.userGroup ug " +
					"WITH ug.defaultGroup = :boolTrue " +
				"INNER JOIN ug.users ugu " +
					"WITH ugu.user.id = :userId " +
				"WHERE cug.credential.id = :credId " +
				"AND cug.privilege = :priv";

		return (UserGroupUser) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setBoolean("boolTrue", true)
				.setLong("userId", userId)
				.setString("priv", priv.name())
				.uniqueResult();
	}
	
	private void saveNewUserToCredentialGroup(long userId, CredentialUserGroup credGroup) {
		if (credGroup == null) {
			throw new NullPointerException();
		}
		saveNewUserToUserGroup(userId, credGroup.getUserGroup(), persistence.currentManager());
	}

	private void saveNewUserToCompetenceGroup(long userId, CompetenceUserGroup compGroup) {
		saveNewUserToCompetenceGroup(userId, compGroup, persistence.currentManager());
	}

	private void saveNewUserToCompetenceGroup(long userId, CompetenceUserGroup compGroup, Session session) {
		if (compGroup == null) {
			throw new NullPointerException();
		}
		saveNewUserToUserGroup(userId, compGroup.getUserGroup(), session);
	}

	@Override
	@Transactional(readOnly = false)
	public void addUserToTheGroup(long groupId, long userId) throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			saveNewUserToUserGroup(userId, group, persistence.currentManager());
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while adding the user to the group");
		}
	}

	private void saveNewUserToUserGroup(long userId, UserGroup userGroup, Session session) {
		try {
			UserGroupUser userGroupUser = new UserGroupUser();
			User u = (User) session.load(User.class, userId);
			userGroupUser.setUser(u);
			userGroupUser.setGroup(userGroup);
			saveEntity(userGroupUser, session);
			session.flush();
		} catch (ConstraintViolationException e) {
			//it means that user is already added to that group and that is ok, it should not be considered as en error
			logger.info("User with id: " + userId + " not added to group because he is already part of the group");
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
				cred, lcd, params));

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
		if (credGroupOptional.isPresent()) {
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
		if (userGroupId > 0) {
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
    public List<ResourceVisibilityMember> getCompetenceVisibilityGroups(long compId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			List<CompetenceUserGroup> compGroups = getCompetenceUserGroups(compId, false,
					privilege, persistence.currentManager());
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
    public List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			String query = "SELECT distinct compGroup FROM CompetenceUserGroup compGroup " +
					   "INNER JOIN fetch compGroup.userGroup userGroup " +
					   "LEFT JOIN fetch userGroup.users users " +
					   "WHERE compGroup.competence.id = :compId " +
					   "AND userGroup.defaultGroup = :defaultGroup ";

			if (privilege != null) {
				query += "AND compGroup.privilege = :priv ";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setBoolean("defaultGroup", true);

			if (privilege != null) {
				q.setString("priv", privilege.name());
			}

			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> defaultGroups = q.list();

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

	@Transactional(readOnly = true)
	private List<CompetenceUserGroup> getCompetenceUserGroups(long compId, boolean returnDefaultGroups,
															  UserGroupPrivilege privilege, Session session)
			throws DbConnectionException {
		try {
			StringBuilder query = new StringBuilder (
							"SELECT compGroup FROM CompetenceUserGroup compGroup " +
							"INNER JOIN fetch compGroup.userGroup userGroup " +
							"WHERE compGroup.competence.id = :compId ");

			if (!returnDefaultGroups) {
				query.append("AND userGroup.defaultGroup = :defaultGroup ");
			}

			if (privilege != null) {
				query.append("AND compGroup.privilege = :priv ");
			}

			Query q = session
					.createQuery(query.toString())
					.setLong("compId", compId);

			if (!returnDefaultGroups) {
				q.setBoolean("defaultGroup", false);
			}
			if (privilege != null) {
				q.setParameter("priv", privilege);
			}

			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = q.list();

			return compGroups;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving competency groups");
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
	public Result<Void> saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups,
													 List<ResourceVisibilityMember> users, long actorId,
													 LearningContextData lcd) throws DbConnectionException {
		try {
			if(groups == null || users == null) {
				throw new NullPointerException("Invalid argument values");
			}
			List<EventData> events = new ArrayList<>();
			events.addAll(saveCompetenceUsers(compId, users, actorId, lcd).getEvents());
			events.addAll(saveCompetenceGroups(compId, groups, actorId, lcd).getEvents());
			Competence1 comp = new Competence1();
			comp.setId(compId);

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
						EventType.RESOURCE_VISIBILITY_CHANGE, actorId, comp, null, lcd, null));
			}

			Result<Void> res = new Result<>();
			res.setEvents(events);
			return res;
		} catch(DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving competency users and groups");
		}
	}

	@Transactional(readOnly = false)
	private Result<Void> saveCompetenceUsers(long compId, List<ResourceVisibilityMember> users, long actorId,
											 LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> res = new Result<>();
			if (users == null) {
				return res;
			}
			if (!users.isEmpty()) {
				//store reference to default groups for learn and edit privilege to be reused for different users
				CompetenceUserGroup learnCompGroup = null;
				CompetenceUserGroup editCompGroup = null;
				for (ResourceVisibilityMember user : users) {
					UserGroupUser userGroupUser = null;
					CompetenceUserGroup compGroup = null;
					switch (user.getStatus()) {
						case CREATED:
							if (user.getPrivilege() == UserGroupPrivilege.Edit) {
								if (editCompGroup == null) {
									Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
											compId, user.getPrivilege(), actorId, context);
									res.addEvents(compUserGroupRes.getEvents());
									editCompGroup = compUserGroupRes.getResult();
								}
								compGroup = editCompGroup;
							} else {
								if (learnCompGroup == null) {
									Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
											compId, user.getPrivilege(), actorId, context);
									res.addEvents(compUserGroupRes.getEvents());
									learnCompGroup = compUserGroupRes.getResult();
								}
								compGroup = learnCompGroup;
							}

							saveNewUserToCompetenceGroup(user.getUserId(), compGroup);
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
			}
			return res;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while saving credential users");
		}
	}

	@Transactional(readOnly = false)
	private Result<Void> saveCompetenceGroups(long compId, List<ResourceVisibilityMember> groups, long userId,
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
								createNewCompetenceUserGroup(group.getGroupId(), false, compId,
										group.getPrivilege(), userId, lcd)
										.getEvents());
						break;
					case REMOVED:
						res.addEvents(
								removeCompetenceUserGroup(compId, group.getId(), group.getGroupId(), userId, lcd)
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

	private Result<Void> removeCompetenceUserGroup(long compId, long compUserGroupId, long userGroupId, long userId,
												   LearningContextData lcd) {
		CompetenceUserGroup compGroup = (CompetenceUserGroup) persistence
				.currentManager().load(CompetenceUserGroup.class, compUserGroupId);
		delete(compGroup);

		Result<Void> res = new Result<>();
		UserGroup userGroup = new UserGroup();
		userGroup.setId(userGroupId);
		Competence1 comp = new Competence1();
		comp.setId(compId);
		Map<String, String> params = new HashMap<>();
		params.put("competenceUserGroupId", compGroup.getId() + "");
		res.addEvent(eventFactory.generateEventData(EventType.USER_GROUP_REMOVED_FROM_RESOURCE, userId, userGroup,
				comp, lcd, params));

		return res;
	}

	private Result<CompetenceUserGroup> getOrCreateDefaultCompetenceUserGroup(long compId, UserGroupPrivilege priv,
																			  long userId, LearningContextData lcd) {
		return getOrCreateDefaultCompetenceUserGroup(compId, priv, userId, lcd, persistence.currentManager());
	}

	private Result<CompetenceUserGroup> getOrCreateDefaultCompetenceUserGroup(long compId, UserGroupPrivilege priv,
																			  long userId, LearningContextData lcd,
																			  Session session) {
		Optional<CompetenceUserGroup> compGroupOptional = getCompetenceDefaultGroup(compId, priv, false,
				session);
		Result<CompetenceUserGroup> res = new Result<>();
		if(compGroupOptional.isPresent()) {
			res.setResult(compGroupOptional.get());
		} else {
			res = createNewCompetenceUserGroup(0, true, compId, priv, userId, lcd, session);
		}
		return res;
	}

	/**
	 * Creates and persists new CredentialUserGroup instance based on existing or new user group.
	 *
	 * @param userGroupId - 0 if new user group should be created
	 * @param isDefault - true if it is a default group
	 * @param compId
	 * @param priv
	 * @param userId
	 * @param lcd
	 * @return
	 */
	private Result<CompetenceUserGroup> createNewCompetenceUserGroup(long userGroupId, boolean isDefault, long compId,
																	 UserGroupPrivilege priv, long userId,
																	 LearningContextData lcd) {
		return createNewCompetenceUserGroup(userGroupId, isDefault, compId, priv, userId, lcd, persistence.currentManager());
	}

	private Result<CompetenceUserGroup> createNewCompetenceUserGroup(long userGroupId, boolean isDefault, long compId,
																	 UserGroupPrivilege priv, long userId,
																	 LearningContextData lcd, Session session) {
		UserGroup userGroup = null;
		if (userGroupId > 0) {
			userGroup = (UserGroup) session.load(UserGroup.class, userGroupId);
		} else {
			userGroup = new UserGroup();
			userGroup.setDefaultGroup(isDefault);
			saveEntity(userGroup, session);
		}
		CompetenceUserGroup compGroup = new CompetenceUserGroup();
		compGroup.setUserGroup(userGroup);
		Competence1 comp = (Competence1) session.load(Competence1.class, compId);
		compGroup.setCompetence(comp);
		compGroup.setPrivilege(priv);
		saveEntity(compGroup, session);

		UserGroup ug = new UserGroup();
		ug.setId(userGroup.getId());
		Competence1 competence = new Competence1();
		competence.setId(compId);
		Map<String, String> params = new HashMap<>();
		params.put("default", isDefault + "");
		params.put("competenceUserGroupId", compGroup.getId() + "");
		EventData ev = eventFactory.generateEventData(EventType.USER_GROUP_ADDED_TO_RESOURCE, userId, ug, competence,
				lcd, params);

		Result<CompetenceUserGroup> res = new Result<>();
		res.setResult(compGroup);
		res.addEvent(ev);

		return res;
	}
	
	private Optional<CompetenceUserGroup> getCompetenceDefaultGroup(long compId, 
			UserGroupPrivilege privilege, boolean returnIfInherited) {
		return getCompetenceDefaultGroup(compId, privilege, returnIfInherited, persistence.currentManager());
	}

	private Optional<CompetenceUserGroup> getCompetenceDefaultGroup(long compId,
																	UserGroupPrivilege privilege,
																	boolean returnIfInherited,
																	Session session) {
		StringBuilder query = new StringBuilder("SELECT compGroup FROM CompetenceUserGroup compGroup " +
				"INNER JOIN compGroup.userGroup userGroup " +
				"WHERE compGroup.competence.id = :compId " +
				"AND compGroup.privilege = :priv " +
				"AND userGroup.defaultGroup = :default ");

		if (!returnIfInherited) {
			query.append("AND compGroup.inherited = :inherited");
		}

		Query q = session
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

    @Transactional
    @Override
    public Result<Void> addLearnPrivilegeToCredentialCompetencesAndGetEvents(long credId, long userId,
																			 long actorId, LearningContextData context,
																			 Session session) {
		Result<Void> res = new Result<>();
		try {
			List<Long> compIds = credManager.getIdsOfAllCompetencesInACredential(credId, session);
			for (long compId : compIds) {
				Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
						compId, UserGroupPrivilege.Learn, actorId, context, session);
				res.addEvents(compUserGroupRes.getEvents());
				saveNewUserToCompetenceGroup(userId, compUserGroupRes.getResult(), session);

				Competence1 comp = new Competence1();
				comp.setId(compId);
				res.addEvent(eventFactory.generateEventData(EventType.RESOURCE_VISIBILITY_CHANGE, actorId,
						comp, null, context, null));
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating user privileges");
		}
	}

	@Override
	@Transactional
	public Result<Void> createCredentialUserGroupAndSaveNewUser(long userId, long credId, UserGroupPrivilege privilege,
														boolean isDefault, long actorId, LearningContextData context)
			throws DbConnectionException {
		try {
			Result<CredentialUserGroup> res = createNewCredentialUserGroup(
					0, isDefault, credId, privilege, actorId, context);
			saveNewUserToCredentialGroup(userId, res.getResult());
			return Result.of(res.getEvents());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving user privilege");
		}
	}

	@Override
	@Transactional
	public Result<Void> createCompetenceUserGroupAndSaveNewUser(long userId, long compId, UserGroupPrivilege privilege,
																boolean isDefault, long actorId, LearningContextData context)
			throws DbConnectionException {
		try {
			Result<CompetenceUserGroup> res = createNewCompetenceUserGroup(
					0, isDefault, compId, privilege, actorId, context);
			saveNewUserToCompetenceGroup(userId, res.getResult());
			return Result.of(res.getEvents());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving user privilege");
		}
	}

}
