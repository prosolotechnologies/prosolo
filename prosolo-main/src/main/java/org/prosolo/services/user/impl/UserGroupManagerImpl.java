package org.prosolo.services.user.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.domainmodel.assessment.AssessorAssignmentMethod;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.*;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.UserGroupData;
import org.prosolo.services.user.data.UserGroupInstructorRemovalMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service("org.prosolo.services.user.UserGroupManager")
public class UserGroupManagerImpl extends AbstractManagerImpl implements UserGroupManager {
	
	private static final long serialVersionUID = 8236179314516077700L;

	private static Logger logger = Logger.getLogger(UserGroupManagerImpl.class);
	
	@Inject private EventFactory eventFactory;
	@Inject private CredentialManager credManager;
	@Inject private UserGroupManager self;
	@Inject private CredentialInstructorManager credentialInstructorManager;
	 
	@Override
	@Transactional(readOnly = true)
	public List<UserGroup> getAllGroups(long orgId, boolean returnDefaultGroups, Session session) throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g " +
				"WHERE g.deleted IS FALSE ";

			if (!returnDefaultGroups) {
				query += "AND g.defaultGroup IS FALSE ";
			}

			if (orgId > 0) {
				query += "AND g.unit.organization.id = :orgId";
			}
			
			Query q = session.createQuery(query);

			if (orgId > 0) {
				q.setLong("orgId", orgId);
			}

			@SuppressWarnings("unchecked")
			List<UserGroup> result = q.list();
			
			return result == null ? new ArrayList<>() : result; 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving user groups");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserGroupData getGroup(long groupgId) throws DbConnectionException {
		try {
			String query = 
				"SELECT g " +
				"FROM UserGroup g " +
				"WHERE g.id = :groupId " +
					"AND g.deleted IS FALSE ";

			UserGroup result = (UserGroup) persistence.currentManager().createQuery(query)
				.setLong("groupId", groupgId)
				.uniqueResult();
			
			return new UserGroupData(result); 
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving group");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserGroupData> searchGroups(long unitId, String searchTerm, int limit, int page)
			throws DbConnectionException {
		try {
			String query = 
				"SELECT g, COUNT(distinct credGroup), COUNT(distinct compGroup) " +
				"FROM UserGroup g " +
				"LEFT JOIN g.credentialUserGroups credGroup " +
				"LEFT JOIN g.competenceUserGroups compGroup " +
				"WHERE g.unit.id = :unitId " +
					"AND g.name LIKE :term " +
					"AND g.deleted IS FALSE " +
				"GROUP BY g " +
				"ORDER BY g.name ASC";
			
			String term = searchTerm == null ? "" : searchTerm;
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager()
					.createQuery(query)
					.setLong("unitId", unitId)
					.setString("term", "%" + term)
					.setFirstResult(page * limit)
					.setMaxResults(limit)
					.list();
			
			if(result == null) {
				return new ArrayList<>();
			}
			List<UserGroupData> groups = new ArrayList<>();
			for(Object[] res : result) {
				UserGroup group = (UserGroup) res[0];
				long credGroupsNo = (long) res[1];
				long compGroupsNo = (long) res[2];
				groups.add(
						new UserGroupData(
								group.getId(), group.getName(),
								(credGroupsNo + compGroupsNo) == 0, group.getUsers().size(),
                                group.getInstructors().size()));
			}
			return groups;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving user groups");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countGroups(long unitId, String searchTerm) throws DbConnectionException {
		try {
			String query = 
				"SELECT COUNT(g.id) " +
				"FROM UserGroup g " +
				"WHERE g.unit.id = :unitId " +
					"AND g.name LIKE :term " +
					"AND g.deleted IS FALSE ";
			
			String term = searchTerm == null ? "" : searchTerm;
			Long result = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("unitId", unitId)
					.setString("term", "%" + term)
					.uniqueResult();
			
			return result == null ? 0 : result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving number of groups");
		}
	}
	
	@Override
	public UserGroup saveNewGroup(long unitId, String name, boolean isDefault,
								  UserContextData context) throws DbConnectionException {
		Result<UserGroup> res = self.saveNewGroupAndGetEvents(unitId, name, isDefault, context);

		eventFactory.generateAndPublishEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<UserGroup> saveNewGroupAndGetEvents(long unitId, String name, boolean isDefault, UserContextData context) throws DbConnectionException {
		try {
			UserGroup group = new UserGroup();
			group.setDateCreated(new Date());
			group.setDefaultGroup(isDefault);
			group.setName(name);
			group.setUnit((Unit) persistence.currentManager().load(Unit.class, unitId));

			saveEntity(group);

			Result<UserGroup> res = new Result<>();
			res.setResult(group);
			UserGroup obj = new UserGroup();
			obj.setId(group.getId());
			res.appendEvent(eventFactory.generateEventData(EventType.Create, context, obj, null, null, null));
			return res;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving user group");
		}
	}

	@Override
	public UserGroup updateGroupName(long groupId, String newName, UserContextData context)
			throws DbConnectionException {
			Result<UserGroup> res = self.updateGroupNameAndGetEvents(groupId, newName, context);

			eventFactory.generateAndPublishEvents(res.getEventQueue());
			return res.getResult();
	}

	@Override
	@Transactional
	public Result<UserGroup> updateGroupNameAndGetEvents(long groupId, String newName, UserContextData context) throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			group.setName(newName);

			Result<UserGroup> res = new Result<>();
			res.setResult(group);
			UserGroup obj = new UserGroup();
			obj.setId(group.getId());
			res.appendEvent(eventFactory.generateEventData(EventType.Edit, context, obj, null, null, null));
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving user group");
		}
	}

	@Override
	public UserGroup updateJoinUrl(long groupId, boolean joinUrlActive, String joinUrlPassword,
								   UserContextData context) {
		Result<UserGroup> res = self.updateJoinUrlAndGetEvents(groupId, joinUrlActive, joinUrlPassword, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional(readOnly = false)
	public Result<UserGroup> updateJoinUrlAndGetEvents(long groupId, boolean joinUrlActive, String joinUrlPassword,
			UserContextData context) {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			group.setJoinUrlActive(joinUrlActive);

			if (joinUrlActive) {
				group.setJoinUrlPassword(joinUrlPassword);
			} else {
				group.setJoinUrlPassword(null);
			}

			Result<UserGroup> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.Edit, context, group, null, null, null));
			res.setResult(group);
			return res;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error saving user group");
		}
	}

	@Override
	public void deleteUserGroup(long id, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.deleteUserGroupAndGetEvents(id, context);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Void> deleteUserGroupAndGetEvents(long id, UserContextData context)
			throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, id);
			//delete(group);
			// we should not delete group from the DB as event processors will need to load it
			markAsDeleted(group);
			
			//generate delete event
			UserGroup deletedGroup = new UserGroup();
			deletedGroup.setId(id);
			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.Delete, context, deletedGroup,null, null, null));
			return res;
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error deleting user group");
		}
	}

	@Override
	public void removeUserFromTheGroup(long groupId, long userId, UserContextData context) throws DbConnectionException {
		Result<Void> result = self.removeUserFromTheGroupAndGetEvents(groupId, userId, context);

		eventFactory.generateAndPublishEvents(result.getEventQueue());
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Void> removeUserFromTheGroupAndGetEvents(long groupId, long userId, UserContextData context)
			throws DbConnectionException {
		try {
			Optional<UserGroupUser> groupUser = getUserGroupUser(groupId, userId);
			if(groupUser.isPresent()) {
				delete(groupUser.get());
			}
			Result<Void> result = new Result<>();
			User u = new User();
			u.setId(userId);
			UserGroup group = new UserGroup();
			group.setId(groupId);

			result.appendEvent(eventFactory.generateEventData(EventType.REMOVE_USER_FROM_GROUP,
					context, u, group, null, null));

			return result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error removing user from the group");
		}
	}

	@Override
	public void removeInstructorFromTheGroup(long groupId, long userId, UserGroupInstructorRemovalMode instructorRemovalMode, UserContextData context) throws DbConnectionException {
		Result<Void> result = self.removeInstructorFromTheGroupAndGetEvents(groupId, userId, instructorRemovalMode, context);
		eventFactory.generateAndPublishEvents(result.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> removeInstructorFromTheGroupAndGetEvents(long groupId, long userId, UserGroupInstructorRemovalMode instructorRemovalMode, UserContextData context) {
		try {
			Optional<UserGroupInstructor> groupInstructor = getUserGroupInstructor(groupId, userId);
			groupInstructor.ifPresent(this::delete);
			Result<Void> result = new Result<>();
			User u = new User();
			u.setId(userId);
			UserGroup group = new UserGroup();
			group.setId(groupId);

            result.appendEvent(eventFactory.generateEventData(EventType.REMOVE_USER_AS_GROUP_INSTRUCTOR,
					context, u, group, null, null));

			result.appendEvents(handleInstructorStatusInExistingCredentialsWhenInstructorIsRemovedFromGroup(
					instructorRemovalMode, groupId, userId, context));

			return result;
		} catch (Exception e) {
			throw new DbConnectionException("Error removing user as instructor from the group", e);
		}
	}

	private EventQueue handleInstructorStatusInExistingCredentialsWhenInstructorIsRemovedFromGroup(UserGroupInstructorRemovalMode instructorRemovalMode,
																   long groupId, long userId, UserContextData context) throws IllegalDataStateException {
		EventQueue events = EventQueue.newEventQueue();
		List<Credential1> notStartedDeliveries = credManager.getDeliveriesWithUserGroupBasedOnStartDate(groupId, false);
		for (Credential1 cred : notStartedDeliveries) {
		    //remove user as instructor from all deliveries not yet started no matter which instructor removal mode is passed
            Optional<Long> credentialInstructorId = credentialInstructorManager.getCredentialInstructorId(cred.getId(), userId);
            if (credentialInstructorId.isPresent()) {
                events.appendEvents(credentialInstructorManager.removeInstructorFromCredentialAndGetEvents(
                        credentialInstructorId.get(),
                        cred.getId(),
                        cred.getAssessorAssignmentMethod() == AssessorAssignmentMethod.AUTOMATIC,
                        context).getEventQueue());
            }
        }
		if (instructorRemovalMode != UserGroupInstructorRemovalMode.LEAVE_AS_INSTRUCTOR) {
			List<Credential1> startedDeliveries = credManager.getDeliveriesWithUserGroupBasedOnStartDate(groupId, true);
			for (Credential1 cred : startedDeliveries) {
				Optional<Long> credentialInstructorId = credentialInstructorManager.getCredentialInstructorId(cred.getId(), userId);
				if (credentialInstructorId.isPresent()) {
					credentialInstructorManager.inactivateCredentialInstructor(credentialInstructorId.get());
					if (instructorRemovalMode == UserGroupInstructorRemovalMode.WITHDRAW_FROM_STUDENTS_WITH_UNSUBMITTED_ASSESSMENT_AND_INACTIVATE) {
						List<TargetCredential1> tCreds = credManager.getTargetCredentialsWithUnsubmittedInstructorAssessment(cred.getId(), userId);
						events.appendEvents(credentialInstructorManager.updateInstructorForStudents(cred.getId(), tCreds, credentialInstructorId.get(),
								cred.getAssessorAssignmentMethod() == AssessorAssignmentMethod.AUTOMATIC, context).getEvents());
					}
				}
			}
		}
		return events;
	}

	private Optional<UserGroupInstructor> getUserGroupInstructor(long groupId, long userId) {
		String query =
				"SELECT gi " +
				"FROM UserGroupInstructor gi " +
				"WHERE gi.user.id = :userId " +
				"AND gi.group.id = :groupId";
		UserGroupInstructor instructor = (UserGroupInstructor) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setLong("userId", userId)
				.uniqueResult();
		return Optional.ofNullable(instructor);
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Void> addUserToGroups(long userId, List<Long> groupIds) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			for(Long group : groupIds) {
				//TODO add context
				Result<Void> r = addUserToTheGroupAndGetEvents(group, userId, UserContextData.empty());
				result.appendEvents(r.getEventQueue());
			}
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error adding user to groups");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> removeUserFromGroups(long userId, List<Long> groupIds, UserContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			for(Long group : groupIds) {
				Result<Void> r = removeUserFromTheGroupAndGetEvents(group, userId, context);
				result.appendEvents(r.getEventQueue());
			}
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error removing user from groups");
		}
	}
	
	@Override
	public void updateUserParticipationInGroups(long userId, List<Long> groupsToRemoveUserFrom, 
			List<Long> groupsToAddUserTo, UserContextData context) throws DbConnectionException {
		Result<Void> result = self.updateUserParticipationInGroupsAndGetEvents(userId,groupsToRemoveUserFrom,groupsToAddUserTo,context);

		eventFactory.generateAndPublishEvents(result.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> updateUserParticipationInGroupsAndGetEvents(long userId, List<Long> groupsToRemoveUserFrom,
																	List<Long> groupsToAddUserTo, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			Result<Void> addResult = addUserToGroups(userId, groupsToAddUserTo);
			Result<Void> removeResult = removeUserFromGroups(userId, groupsToRemoveUserFrom, context);

			result.appendEvents(addResult.getEventQueue());
			result.appendEvents(removeResult.getEventQueue());

			return result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error updating user groups");
		}

	}

	@Override
	@Transactional(readOnly = true)
    public long getNumberOfUsersInAGroup(long groupId) throws DbConnectionException {
    	try {
    		String query =
					"SELECT COUNT(groupUser.id) " +
					"FROM UserGroupUser groupUser " +
					"WHERE groupUser.group.id = :groupId";
			Long count = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.uniqueResult();
			return count == null ? 0 : count;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving number of users in a group");
    	}
    }

	@Override
	@Transactional(readOnly = true)
	public UserGroupData getUserGroupDataWithUserCountAndCanBeDeletedInfo(long groupId) throws DbConnectionException {
		try {
			String query =
					"SELECT g, COUNT(distinct credGroup), COUNT(distinct compGroup) " +
					"FROM UserGroup g " +
					"LEFT JOIN g.credentialUserGroups credGroup " +
					"LEFT JOIN g.competenceUserGroups compGroup " +
					"WHERE g.id = :groupId " +
						"AND g.deleted IS FALSE ";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.uniqueResult();

			UserGroup userGroup = (UserGroup) res[0];
			long credGroupsCount = (long) res[1];
			long compGroupsCount = (long) res[2];

			return new UserGroupData(
                    userGroup.getId(),
                    userGroup.getName(),
                    (credGroupsCount + compGroupsCount) == 0,
                    userGroup.getUsers().size(),
                    userGroup.getInstructors().size());
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error retrieving user group data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
    public boolean isUserInGroup(long groupId, long userId) throws DbConnectionException {
    	try {
    		String query =
					"SELECT groupUser.id " +
					"FROM UserGroupUser groupUser " +
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
    		throw new DbConnectionException("Error checking if user belongs to a group");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CredentialUserGroup> getCredentialUserGroups(long groupId) throws DbConnectionException {
    	try {
    		String query =
					"SELECT credGroup " +
					"FROM CredentialUserGroup credGroup " +
					"LEFT JOIN credGroup.userGroup g " +
					"WHERE g.id = :groupId " +
						"AND g.deleted IS FALSE ";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.list();
			return credGroups == null ? new ArrayList<>() : credGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving credential groups");
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
    		String query =
					"SELECT credGroup " +
					"FROM CredentialUserGroup credGroup " +
					"WHERE credGroup.credential.id = :credId " +
						"AND credGroup.userGroup.deleted IS FALSE ";
			@SuppressWarnings("unchecked")
			List<CredentialUserGroup> credGroups = session
					.createQuery(query)
					.setLong("credId", credId)
					.list();
			return credGroups == null ? new ArrayList<>() : credGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving credential groups");
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
    		String query =
					"SELECT compGroup " +
					"FROM CompetenceUserGroup compGroup " +
					"WHERE compGroup.competence.id = :compId " +
						"AND compGroup.userGroup.deleted IS FALSE ";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = session
					.createQuery(query)
					.setLong("compId", compId)
					.list();
			return compGroups == null ? new ArrayList<>() : compGroups;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<CompetenceUserGroup> getCompetenceUserGroups(long groupId)
			throws DbConnectionException {
    	try {
    		String query =
					"SELECT compGroup " +
					"FROM CompetenceUserGroup compGroup " +
					"LEFT JOIN compGroup.userGroup g " +
					"WHERE g.id = :groupId " +
						"AND g.deleted IS FALSE ";
			@SuppressWarnings("unchecked")
			List<CompetenceUserGroup> compGroups = persistence.currentManager()
					.createQuery(query)
					.setLong("groupId", groupId)
					.list();
			return compGroups == null ? new ArrayList<>() : compGroups;
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving competence groups");
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
							group.getPrivilege(), false, true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving credential groups");
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
    private List<CredentialUserGroup> getCredentialUserGroups (long credId, boolean returnDefaultGroups,
    		UserGroupPrivilege privilege, Session session) throws DbConnectionException {
		try {
    		StringBuilder query = new StringBuilder (
    				"SELECT credGroup " +
					"FROM CredentialUserGroup credGroup " +
					"INNER JOIN fetch credGroup.userGroup userGroup " +
					"WHERE credGroup.credential.id = :credId " +
						"AND userGroup.deleted IS FALSE ");
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
    		throw new DbConnectionException("Error retrieving credential groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCredentialVisibilityUsers(long credId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			StringBuilder query = new StringBuilder(
					"SELECT distinct credGroup FROM CredentialUserGroup credGroup " +
					"INNER JOIN fetch credGroup.userGroup userGroup " +
					"LEFT JOIN fetch userGroup.users users " +
					"WHERE credGroup.credential.id = :credId " +
						"AND userGroup.defaultGroup = :defaultGroup " +
						"AND userGroup.deleted IS FALSE ");

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
								group.getPrivilege(), false, true));
					}
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving credential users");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> saveCredentialUsersAndGroups(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, Optional<UserGroupInstructorRemovalMode> instructorRemovalMode, UserContextData context) throws DbConnectionException {
    	try {
    		if(groups == null || users == null) {
    			throw new NullPointerException("Invalid argument values");
    		}
    		EventQueue events = EventQueue.newEventQueue();
    		events.appendEvents(saveCredentialUsers(credId, users, context).getEventQueue());
    		events.appendEvents(saveCredentialGroups(credId, groups, instructorRemovalMode, context).getEventQueue());
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
    			events.appendEvent(eventFactory.generateEventData(
        				EventType.RESOURCE_VISIBILITY_CHANGE, context, cred,null, null, null));
    		}
    		
    		Result<Void> res = new Result<>();
    		res.setEventQueue(events);
    		return res;
    	} catch(DbConnectionException dce) {
    		throw dce;
    	} catch(Exception e) {
    		logger.error("error", e);
    		throw new DbConnectionException("Error saving credential users and groups", e);
    	}
    }
	
    private Result<Void> saveCredentialUsers(long credId, List<ResourceVisibilityMember> users,
											 UserContextData context) throws DbConnectionException {
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
											credId, user.getPrivilege(), context);
									res.appendEvents(credUserGroupRes.getEventQueue());
									editCredGroup = credUserGroupRes.getResult();
								}
								credGroup = editCredGroup;
							} else {
								if (learnCredGroup == null) {
									Result<CredentialUserGroup> credUserGroupRes = getOrCreateDefaultCredentialUserGroup(
											credId, user.getPrivilege(), context);
									res.appendEvents(credUserGroupRes.getEventQueue());
									learnCredGroup = credUserGroupRes.getResult();
								}
								credGroup = learnCredGroup;
							}
							saveNewUserToCredentialGroup(user.getUserId(), credGroup);
							generateUserGroupChangeEventIfNotGenerated(credGroup.getUserGroup().getId(),
									context, userGroupsChangedEvents);
							break;
	    				case REMOVED:
	    					userGroupUser = (UserGroupUser) persistence
								.currentManager().load(UserGroupUser.class, user.getId());
	    					delete(userGroupUser);
	    					generateUserGroupChangeEventIfNotGenerated(userGroupUser.getGroup().getId(),
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
	    		res.getEventQueue().getEvents().stream()
	    			.filter(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE)
	    			.map(ev -> ev.getObject().getId())
	    			.forEach(id -> userGroupsChangedEvents.remove(id));

	    		//generate all user group change events
	    		userGroupsChangedEvents.forEach((key, event) -> res.appendEvent(event));
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving credential users");
    	}
    }

	private void generateUserGroupChangeEventIfNotGenerated(long userGroupId,
															UserContextData context,
															Map<Long, EventData> userGroupsChangedEvents) {
		UserGroup ug = new UserGroup();
		ug.setId(userGroupId);
		//if event for this user group is not already generated, generate it and put it in the map
		if(userGroupsChangedEvents.get(userGroupId) == null) {
			userGroupsChangedEvents.put(userGroupId,
					eventFactory.generateEventData(
							EventType.USER_GROUP_CHANGE, context, ug, null, null, null));
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Result<Void> saveUserToDefaultCredentialGroupAndGetEvents(long userId, long credId,
																	 UserGroupPrivilege privilege,
																	 UserContextData context)
			throws DbConnectionException {
		try {
			Result<CredentialUserGroup> credGroup = getOrCreateDefaultCredentialUserGroup(credId,
					privilege, context);
			saveNewUserToCredentialGroup(userId, credGroup.getResult());
			Result<Void> res = new Result<>();
			res.setEventQueue(credGroup.getEventQueue());
			boolean groupJustCreated = res.getEventQueue().getEvents().stream()
				.anyMatch(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE);
			//if user group is not just created, generate add user to group event
			if (!groupJustCreated) {
				User object = new User();
				object.setId(userId);
				UserGroup target = new UserGroup();
				target.setId(credGroup.getResult().getUserGroup().getId());
				res.appendEvent(eventFactory.generateEventData(
						EventType.ADD_USER_TO_GROUP, context, object, target, null, null));
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error adding privilege to a user for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Result<Void> removeUserFromDefaultCredentialGroupAndGetEvents(long userId, long credId,
																	 UserGroupPrivilege privilege,
																	 UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			UserGroupUser ugu = getUserFromDefaultCredentialUserGroup(userId, credId, privilege);
			if (ugu != null) {
				result.appendEvents(removeUserFromGroupAndGetEvents(ugu, context).getEventQueue());
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error removing privilege for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Result<Void> saveUserToDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																	 UserGroupPrivilege privilege,
																	 UserContextData context)
			throws DbConnectionException {
		try {
			Result<CompetenceUserGroup> compGroup = getOrCreateDefaultCompetenceUserGroup(
					compId, privilege, context);
			saveNewUserToCompetenceGroup(userId, compGroup.getResult());
			Result<Void> res = new Result<>();
			res.appendEvents(compGroup.getEventQueue());
			boolean groupJustCreated = res.getEventQueue().getEvents().stream()
					.anyMatch(ev -> ev.getEventType() == EventType.USER_GROUP_ADDED_TO_RESOURCE);
			//if user group is not just created, generate add user to group event
			if (!groupJustCreated) {
				User object = new User();
				object.setId(userId);
				UserGroup target = new UserGroup();
				target.setId(compGroup.getResult().getUserGroup().getId());
				res.appendEvent(eventFactory.generateEventData(
						EventType.ADD_USER_TO_GROUP, context, object, target, null, null));
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error adding privilege to a user for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Result<Void> removeUserFromDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																		 UserGroupPrivilege privilege, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			UserGroupUser ugu = getUserFromDefaultCompetenceUserGroup(userId, compId, privilege);
			if (ugu != null) {
				result.appendEvents(removeUserFromGroupAndGetEvents(ugu, context).getEventQueue());
			}

			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error removing privilege for credential");
		}
	}

	private UserGroupUser getUserFromDefaultCompetenceUserGroup(long userId, long compId, UserGroupPrivilege priv) {
		String query =
				"SELECT ugu " +
				"FROM CompetenceUserGroup cug " +
				"INNER JOIN cug.userGroup ug " +
				"WITH ug.defaultGroup = :boolTrue " +
				"INNER JOIN ug.users ugu " +
				"WITH ugu.user.id = :userId " +
				"WHERE cug.competence.id = :compId " +
					"AND cug.privilege = :priv " +
					"AND cug.inherited = :boolFalse " +
					"AND ug.deleted IS FALSE ";

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
		String query =
				"SELECT ugu FROM CredentialUserGroup cug " +
				"INNER JOIN cug.userGroup ug " +
					"WITH ug.defaultGroup = :boolTrue " +
				"INNER JOIN ug.users ugu " +
					"WITH ugu.user.id = :userId " +
				"WHERE cug.credential.id = :credId " +
					"AND cug.privilege = :priv " +
					"AND ug.deleted IS FALSE ";

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
	@Transactional
	public Result<Void> addUserToTheGroupAndGetEvents(long groupId, long userId, UserContextData context) throws DbConnectionException {
		try {
			UserGroup group = (UserGroup) persistence.currentManager().load(UserGroup.class, groupId);
			saveNewUserToUserGroup(userId, group, persistence.currentManager());

			Result<Void> res = new Result<>();

			//TODO don't generate event if user was already added to the group
			UserGroup ug = new UserGroup();
			ug.setId(groupId);
			User u = new User();
			u.setId(userId);
			res.appendEvent(eventFactory.generateEventData(
					EventType.ADD_USER_TO_GROUP, context, u, ug, null, null));
			return res;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error adding the user to the group");
		}
	}

	@Override
	public void addUserToTheGroup(long groupId, long userId, UserContextData context) throws DbConnectionException {
		Result<Void> result = self.addUserToTheGroupAndGetEvents(groupId, userId, context);

		eventFactory.generateAndPublishEvents(result.getEventQueue());
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

	@Override
	public void addInstructorToTheGroup(long groupId, long userId, UserContextData context) {
		Result<Void> result = self.addInstructorToTheGroupAndGetEvents(groupId, userId, context);
		eventFactory.generateAndPublishEvents(result.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> addInstructorToTheGroupAndGetEvents(long groupId, long userId, UserContextData context) {
		try {
			UserGroup group = loadResource(UserGroup.class, groupId);
			User u = loadResource(User.class, userId);
			UserGroupInstructor groupInstructor = new UserGroupInstructor();
			groupInstructor.setUser(u);
			groupInstructor.setGroup(group);

			Result<Void> res = new Result<>();
			boolean addedToGroup = false;
			try {
				saveEntity(groupInstructor);

				UserGroup ug = new UserGroup();
				ug.setId(groupId);
				User user = new User();
				user.setId(userId);
				res.appendEvent(eventFactory.generateEventData(
						EventType.ADD_USER_AS_GROUP_INSTRUCTOR, context, user, ug, null, null));

				addedToGroup = true;
			} catch (ConstraintViolationException e) {
				//it means that user is already added to that group and that is ok, it should not be considered as en error
				logger.info("User with id: " + userId + " not added to group as instructor because he is already part of the group");
			}

			if (addedToGroup) {
				res.appendEvents(handleInstructorStatusInExistingCredentialsWhenInstructorIsAddedToGroup(
						groupId, userId, context));
			}
			return res;
		} catch(Exception e) {
			throw new DbConnectionException("Error adding the user to the group", e);
		}
	}

	private EventQueue handleInstructorStatusInExistingCredentialsWhenInstructorIsAddedToGroup(
			long groupId, long userId, UserContextData context) {
		EventQueue events = EventQueue.newEventQueue();
		List<CredentialUserGroup> credentialUserGroups = getCredentialUserGroups(groupId);
		for (CredentialUserGroup credGroup : credentialUserGroups) {
			events.appendEvents(credentialInstructorManager.addOrActivateCredentialInstructorAndGetEvents(
					credGroup.getCredential().getId(), userId, context).getEventQueue());
		}
		return events;
	}

	private Result<Void> removeUserFromGroupAndGetEvents(UserGroupUser userGroupUser,
														 UserContextData context) {
		Result<Void> res = new Result<>();
		
		User object = new User();
		object.setId(userGroupUser.getUser().getId());
		UserGroup target = new UserGroup();
		target.setId(userGroupUser.getGroup().getId());
		res.appendEvent(eventFactory.generateEventData(
				EventType.REMOVE_USER_FROM_GROUP, context, object, target,null, null));
		delete(userGroupUser);
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
    public boolean isUserInADefaultCredentialGroup(long userId, long credId) 
    		throws DbConnectionException {
		try {
			String query =
					"SELECT user.id FROM CredentialUserGroup credGroup " +
					"INNER JOIN credGroup.userGroup userGroup " +
					"INNER JOIN userGroup.users user " +
						"WITH user.id = :userId " +
					"WHERE credGroup.credential.id = :credId " +
						"AND userGroup.defaultGroup = :defaultGroup " +
						"AND userGroup.deleted IS FALSE ";
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
    		throw new DbConnectionException("Error checking if user is in a default credential group");
    	}
    }
	
    private Result<Void> saveCredentialGroups(long credId, List<ResourceVisibilityMember> groups, Optional<UserGroupInstructorRemovalMode> instructorRemovalMode, UserContextData context)
			throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		
    		if (groups == null) {
    			return res;
    		}
    		
    		for (ResourceVisibilityMember group : groups) {
    			switch(group.getStatus()) {
    				case CREATED:
    					res.appendEvents(
    							createNewCredentialUserGroup(group.getGroupId(), false, credId, group.getPrivilege(), context)
									.getEventQueue());
    					break;
    				case REMOVED:
    					res.appendEvents(
    							removeCredentialUserGroup(credId, group.getId(), group.getGroupId(), instructorRemovalMode, context)
    								.getEventQueue());
    					break;
    				case UP_TO_DATE:
    					break;
    					
    			}
    		}
    		return res;
    	} catch(Exception e) {
    		throw new DbConnectionException("Error saving credential groups", e);
    	}
    }
	
	private Result<Void> removeCredentialUserGroup(long credId, long credUserGroupId, long userGroupId, Optional<UserGroupInstructorRemovalMode> instructorRemovalMode, UserContextData context) throws ResourceCouldNotBeLoadedException, IllegalDataStateException {
		CredentialUserGroup credGroup = (CredentialUserGroup) persistence
				.currentManager().load(CredentialUserGroup.class, credUserGroupId);
		UserGroupPrivilege privilege = credGroup.getPrivilege();
		Result<Void> res = new Result<>();
		UserGroup userGroup = new UserGroup();
		userGroup.setId(userGroupId);
		Credential1 cred = new Credential1();
		cred.setId(credId);
		Map<String, String> params = new HashMap<>();
		params.put("credentialUserGroupId", credGroup.getId() + "");
		params.put("privilege", privilege.name());
		res.appendEvent(eventFactory.generateEventData(EventType.USER_GROUP_REMOVED_FROM_RESOURCE, context, userGroup, cred, null, params));

		delete(credGroup);

		if (privilege == UserGroupPrivilege.Learn) {
            //remove or inactivate group instructors as credential instructors if needed; concerns only group with learn privilege
            res.appendEvents(handleInstructorsStatusInCredentialOnGroupRemove(credId, userGroupId, instructorRemovalMode, context));
        }
		return res;
	}

	private EventQueue handleInstructorsStatusInCredentialOnGroupRemove(
	        long credentialId, long groupId, Optional<UserGroupInstructorRemovalMode> instructorRemovalMode, UserContextData context) throws ResourceCouldNotBeLoadedException, IllegalDataStateException {
		EventQueue events = EventQueue.newEventQueue();
		Credential1 cred = loadResource(Credential1.class, credentialId);
		//if delivery is not started we should remove instructors from credential
		if (cred.getDeliveryStart() == null || cred.getDeliveryStart().after(new Date())) {
			List<Long> groupInstructorsUserIds = getGroupInstructorsUserIds(groupId);
			for (long userId : groupInstructorsUserIds) {
				Optional<Long> credentialInstructorId = credentialInstructorManager.getCredentialInstructorId(credentialId, userId);
				if (credentialInstructorId.isPresent()) {
					events.appendEvents(credentialInstructorManager.removeInstructorFromCredentialAndGetEvents(
							credentialInstructorId.get(),
							credentialId,
							cred.getAssessorAssignmentMethod() == AssessorAssignmentMethod.AUTOMATIC,
							context).getEventQueue());
				}
			}
		} else {
			UserGroupInstructorRemovalMode instructorRemovalModeVal = instructorRemovalMode.get();
			if (instructorRemovalModeVal != UserGroupInstructorRemovalMode.LEAVE_AS_INSTRUCTOR) {
				List<Long> groupInstructorsUserIds = getGroupInstructorsUserIds(groupId);
				for (long userId : groupInstructorsUserIds) {
					Optional<Long> credentialInstructorId = credentialInstructorManager.getCredentialInstructorId(credentialId, userId);
					if (credentialInstructorId.isPresent()) {
						credentialInstructorManager.inactivateCredentialInstructor(credentialInstructorId.get());
						if (instructorRemovalModeVal == UserGroupInstructorRemovalMode.WITHDRAW_FROM_STUDENTS_WITH_UNSUBMITTED_ASSESSMENT_AND_INACTIVATE) {
							List<TargetCredential1> tCreds = credManager.getTargetCredentialsWithUnsubmittedInstructorAssessment(cred.getId(), userId);
							events.appendEvents(credentialInstructorManager.updateInstructorForStudents(cred.getId(), tCreds, credentialInstructorId.get(),
									cred.getAssessorAssignmentMethod() == AssessorAssignmentMethod.AUTOMATIC, context).getEvents());
						}
					}
				}
			}
		}
		return events;
	}
	
	private Result<CredentialUserGroup> getOrCreateDefaultCredentialUserGroup(long credId, UserGroupPrivilege priv,
			UserContextData context) {
		Optional<CredentialUserGroup> credGroupOptional = getCredentialDefaultGroup(credId, priv);
		Result<CredentialUserGroup> res = new Result<>();
		if (credGroupOptional.isPresent()) {
			res.setResult(credGroupOptional.get());
		} else {
			res = createNewCredentialUserGroup(0, true, credId, priv, context);
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
	 * @param context
	 * @return
	 */
	private Result<CredentialUserGroup> createNewCredentialUserGroup(long userGroupId, boolean isDefault, long credId, 
			UserGroupPrivilege priv, UserContextData context) {
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
		Result<CredentialUserGroup> res = new Result<>();
		res.appendEvent(eventFactory.generateEventData(EventType.USER_GROUP_ADDED_TO_RESOURCE, context, ug,
				credential, null, params));

		if (priv == UserGroupPrivilege.Learn && userGroupId > 0 && !isDefault) {
			//add group instructors to credential as instructors; concerns only group with learn privilege
			res.appendEvents(handleInstructorsStatusInCredentialOnGroupAdd(userGroupId, credId, context));
		}

		res.setResult(credGroup);

		return res;
	}

	private EventQueue handleInstructorsStatusInCredentialOnGroupAdd(
			long groupId, long credentialId, UserContextData context) {
		EventQueue events = EventQueue.newEventQueue();
		List<Long> groupInstructorsUserIds = getGroupInstructorsUserIds(groupId);
		for (long userId : groupInstructorsUserIds) {
			events.appendEvents(credentialInstructorManager.addOrActivateCredentialInstructorAndGetEvents(
					credentialId, userId, context).getEventQueue());
		}
		return events;
	}

	private List<Long> getGroupInstructorsUserIds(long groupId) {
		String query =
				"SELECT u.id " +
				"FROM UserGroupInstructor ugi " +
				"INNER JOIN ugi.user u " +
				"WHERE ugi.group.id = :groupId";

		return (List<Long>) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.list();
	}
	
	private Optional<CredentialUserGroup> getCredentialDefaultGroup(long credId, UserGroupPrivilege privilege) {
		String query =
				"SELECT credGroup FROM CredentialUserGroup credGroup " +
				"INNER JOIN credGroup.userGroup userGroup " +
				"WHERE credGroup.credential.id = :credId " +
					"AND credGroup.privilege = :priv " +
					"AND userGroup.defaultGroup = :default " +
					"AND userGroup.deleted IS FALSE ";

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
			List<UserGroup> compGroups = getCompetenceUserGroups(compId, false,
					privilege, persistence.currentManager());
			if (compGroups != null) {
				for (UserGroup group : compGroups) {
					//check if user group is inherited and get comp group id
					String query = "SELECT compGroup " +
							       "FROM CompetenceUserGroup compGroup " +
								   "INNER JOIN compGroup.userGroup userGroup " +
							       "WHERE compGroup.competence.id = :compId " +
										"AND userGroup.id = :userGroupId " +
										"AND userGroup.defaultGroup IS FALSE " +
										"AND compGroup.privilege = :priv " +
										"AND userGroup.deleted IS FALSE " +
								   "ORDER BY CASE WHEN compGroup.inherited IS TRUE THEN 1 ELSE 2 END";

					CompetenceUserGroup compGroup = (CompetenceUserGroup) persistence.currentManager()
							.createQuery(query)
							.setLong("compId", compId)
							.setLong("userGroupId", group.getId())
							.setString("priv", privilege.name())
							.setMaxResults(1)
							.uniqueResult();

					members.add(new ResourceVisibilityMember(compGroup.getId(), group.getId(),
							group.getName(), group.getUsers().size(), compGroup.getPrivilege(),
							compGroup.isInherited(), true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving competence groups");
    	}
    }
	
	@Override
	@Transactional(readOnly = true)
    public List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId, UserGroupPrivilege privilege)
    		throws DbConnectionException {
    	List<ResourceVisibilityMember> members = new ArrayList<>();
		try {
			String query =
					"SELECT distinct user FROM CompetenceUserGroup compGroup " +
					"INNER JOIN compGroup.userGroup userGroup " +
					"INNER JOIN userGroup.users userGroupUser " +
					"INNER JOIN userGroupUser.user user " +
					"WHERE compGroup.competence.id = :compId " +
						"AND userGroup.defaultGroup IS TRUE " +
						"AND compGroup.privilege = :priv " +
						"AND userGroup.deleted IS FALSE ";

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setString("priv", privilege.name());

			@SuppressWarnings("unchecked")
			List<User> users = q.list();

			if (users != null) {
				for (User user : users) {
					String q1 =
							"SELECT user, compGroup.inherited FROM CompetenceUserGroup compGroup " +
							"INNER JOIN compGroup.userGroup userGroup " +
							"INNER JOIN userGroup.users user " +
							"WITH user.user.id = :userId " +
							"WHERE compGroup.competence.id = :compId " +
								"AND userGroup.defaultGroup IS TRUE " +
								"AND compGroup.privilege = :priv " +
								"AND userGroup.deleted IS FALSE " +
							"ORDER BY CASE WHEN compGroup.inherited IS TRUE THEN 1 ELSE 2 END";

					Object[] res = (Object[]) persistence.currentManager().createQuery(q1)
							.setLong("userId", user.getId())
							.setLong("compId", compId)
							.setString("priv", privilege.name())
							.setMaxResults(1)
							.uniqueResult();

					UserGroupUser ugu = (UserGroupUser) res[0];
					boolean inherited = (boolean) res[1];
					members.add(new ResourceVisibilityMember(ugu.getId(), user,
							privilege, inherited,true));
				}
			}
			
			return members;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error retrieving competence users");
    	}
    }

	private List<UserGroup> getCompetenceUserGroups(long compId, boolean returnDefaultGroups,
															  UserGroupPrivilege privilege, Session session)
			throws DbConnectionException {
		try {
			StringBuilder query = new StringBuilder (
							"SELECT DISTINCT userGroup " +
							"FROM CompetenceUserGroup compGroup " +
							"INNER JOIN compGroup.userGroup userGroup " +
							"WHERE compGroup.competence.id = :compId " +
								"AND compGroup.privilege = :priv " +
								"AND userGroup.deleted IS FALSE ");

			if (!returnDefaultGroups) {
				query.append("AND userGroup.defaultGroup = :defaultGroup ");
			}

			Query q = session
					.createQuery(query.toString())
					.setLong("compId", compId)
					.setParameter("priv", privilege);

			if (!returnDefaultGroups) {
				q.setBoolean("defaultGroup", false);
			}

			@SuppressWarnings("unchecked")
			List<UserGroup> compGroups = q.list();

			return compGroups;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving competency groups");
		}
	}

	private Optional<UserGroupUser> getUserGroupUser(long groupId, long userId) {
		String query =
				"SELECT groupUser " +
				"FROM UserGroupUser groupUser " +
				"LEFT JOIN groupUser.group g " +
				"WHERE groupUser.user.id = :userId " +
					"AND g.id = :groupId " +
					"AND g.deleted IS FALSE ";
		UserGroupUser groupUser = (UserGroupUser) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setLong("userId", userId)
				.uniqueResult();
		return groupUser != null ? Optional.of(groupUser) : Optional.empty();
	}

	@Override
	@Transactional
	public Result<Void> saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups,
													 List<ResourceVisibilityMember> users, UserContextData context)
			throws DbConnectionException {
		try {
			if(groups == null || users == null) {
				throw new NullPointerException("Invalid argument values");
			}
			EventQueue events = EventQueue.newEventQueue();
			events.appendEvents(saveCompetenceUsers(compId, users, context).getEventQueue());
			events.appendEvents(saveCompetenceGroups(compId, groups, context).getEventQueue());
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
				events.appendEvent(eventFactory.generateEventData(
						EventType.RESOURCE_VISIBILITY_CHANGE, context, comp, null, null, null));
			}

			Result<Void> res = new Result<>();
			res.setEventQueue(events);
			return res;
		} catch(DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error saving competency users and groups");
		}
	}

	private Result<Void> saveCompetenceUsers(long compId, List<ResourceVisibilityMember> users, UserContextData context)
			throws DbConnectionException {
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
					UserGroupUser userGroupUser;
					CompetenceUserGroup compGroup;
					switch (user.getStatus()) {
						case CREATED:
							if (user.getPrivilege() == UserGroupPrivilege.Edit) {
								if (editCompGroup == null) {
									Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
											compId, user.getPrivilege(), context);
									res.appendEvents(compUserGroupRes.getEventQueue());
									editCompGroup = compUserGroupRes.getResult();
								}
								compGroup = editCompGroup;
							} else {
								if (learnCompGroup == null) {
									Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
											compId, user.getPrivilege(), context);
									res.appendEvents(compUserGroupRes.getEventQueue());
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
			throw new DbConnectionException("Error saving credential users");
		}
	}

	private Result<Void> saveCompetenceGroups(long compId, List<ResourceVisibilityMember> groups, UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> res = new Result<>();

			if (groups == null) {
				return res;
			}

			for (ResourceVisibilityMember group : groups) {
				switch(group.getStatus()) {
					case CREATED:
						res.appendEvents(
								createNewCompetenceUserGroup(group.getGroupId(), false, compId,
										group.getPrivilege(), context)
										.getEventQueue());
						break;
					case REMOVED:
						res.appendEvents(
								removeCompetenceUserGroup(compId, group.getId(),
										group.getGroupId(), context).getEventQueue());
						break;
					case UP_TO_DATE:
						break;

				}
			}
			return res;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error saving credential groups");
		}
	}

	private Result<Void> removeCompetenceUserGroup(long compId, long compUserGroupId, long userGroupId,
												   UserContextData context) {
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
		res.appendEvent(eventFactory.generateEventData(EventType.USER_GROUP_REMOVED_FROM_RESOURCE,
				context, userGroup, comp, null, params));

		return res;
	}

	private Result<CompetenceUserGroup> getOrCreateDefaultCompetenceUserGroup(long compId, UserGroupPrivilege priv,
																			  UserContextData context) {
		return getOrCreateDefaultCompetenceUserGroup(compId, priv, context, persistence.currentManager());
	}

	private Result<CompetenceUserGroup> getOrCreateDefaultCompetenceUserGroup(long compId, UserGroupPrivilege priv,
																			  UserContextData context, Session session) {
		Optional<CompetenceUserGroup> compGroupOptional = getCompetenceDefaultGroup(compId, priv, false,
				session);
		Result<CompetenceUserGroup> res = new Result<>();
		if(compGroupOptional.isPresent()) {
			res.setResult(compGroupOptional.get());
		} else {
			res = createNewCompetenceUserGroup(0, true, compId, priv, context, session);
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
	 * @param context
	 * @return
	 */
	private Result<CompetenceUserGroup> createNewCompetenceUserGroup(long userGroupId, boolean isDefault, long compId,
																	 UserGroupPrivilege priv, UserContextData context) {
		return createNewCompetenceUserGroup(userGroupId, isDefault, compId, priv, context, persistence.currentManager());
	}

	private Result<CompetenceUserGroup> createNewCompetenceUserGroup(long userGroupId, boolean isDefault, long compId,
																	 UserGroupPrivilege priv, UserContextData context,
																	 Session session) {
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
		EventData ev = eventFactory.generateEventData(EventType.USER_GROUP_ADDED_TO_RESOURCE, context, ug, competence, null, params);

		Result<CompetenceUserGroup> res = new Result<>();
		res.setResult(compGroup);
		res.appendEvent(ev);

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
		StringBuilder query = new StringBuilder(
				"SELECT compGroup FROM CompetenceUserGroup compGroup " +
				"INNER JOIN compGroup.userGroup userGroup " +
				"WHERE compGroup.competence.id = :compId " +
					"AND compGroup.privilege = :priv " +
					"AND userGroup.defaultGroup = :default " +
					"AND userGroup.deleted IS FALSE ");

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
    		UserContextData context, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		res.appendEvents(removeUserGroupPrivilegeFromCompetencesAndGetEvents(credId, userGroupId, context, session)
    				.getEventQueue());
    		res.appendEvents(removeUserGroupPrivilegeFromDeliveriesAndGetEvents(credId, userGroupId, context, session)
    				.getEventQueue());
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
    private Result<Void> removeUserGroupPrivilegeFromCompetencesAndGetEvents(long credId, long userGroupId,
    		UserContextData context, Session session) throws DbConnectionException {
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
	        		res.appendEvent(eventFactory.generateEventData(
	        				EventType.RESOURCE_VISIBILITY_CHANGE, context, comp, null, null, null));
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
    private Result<Void> removeUserGroupPrivilegeFromDeliveriesAndGetEvents(long credId, long userGroupId,
    		UserContextData context, Session session) throws DbConnectionException {
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
	        		res.appendEvent(eventFactory.generateEventData(
	        				EventType.RESOURCE_VISIBILITY_CHANGE, context, del, null, null,null));
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(long compId, long credId, 
    		UserContextData context, Session session) throws DbConnectionException {
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
    		res.appendEvent(eventFactory.generateEventData(
    				EventType.RESOURCE_VISIBILITY_CHANGE, context, comp, null, null, null));
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupPrivilegeFromCredentialAndGetEvents(long credUserGroupId,
    		UserContextData context, Session session) throws DbConnectionException {
		Result<Void> res = new Result<>();
		res.appendEvents(propagateUserGroupPrivilegeFromCredentialToAllCompetencesAndGetEvents(
				credUserGroupId, context, session).getEventQueue());
		res.appendEvents(propagateUserGroupPrivilegeFromCredentialToAllDeliveriesAndGetEvents(
				credUserGroupId, context, session).getEventQueue());
		return res;
    }
	
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToAllCompetencesAndGetEvents(long credUserGroupId,
    		UserContextData context, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		CredentialUserGroup credUserGroup = (CredentialUserGroup) session.load(CredentialUserGroup.class, credUserGroupId);
    		List<Long> compIds = credManager.getIdsOfAllCompetencesInACredential(credUserGroup.getCredential().getId(), session);
    		for (long compId : compIds) {
    			res.appendEvents(propagateUserGroupPrivilegeFromCredential(credUserGroup, compId, context, session).getEventQueue());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
	/**
	 * Propagates privilege to all credential deliveries but only if it is edit privilege.
	 * 
	 * @param credUserGroupId
	 * @param context
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToAllDeliveriesAndGetEvents(long credUserGroupId,
    		UserContextData context, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		CredentialUserGroup credUserGroup = (CredentialUserGroup) session.load(CredentialUserGroup.class, 
    				credUserGroupId);
    		//only edit privileges are propagated to deliveries
    		if(credUserGroup.getPrivilege() == UserGroupPrivilege.Edit) {
	    		List<Long> deliveries = credManager.getIdsOfAllCredentialDeliveries(
	    				credUserGroup.getCredential().getId(), session);
	    		for (long deliveryId : deliveries) {
	    			res.appendEvents(propagateUserGroupPrivilegeFromCredentialToDelivery(credUserGroup, deliveryId,
							context, session).getEventQueue());
	    		}
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupPrivilegesFromCredentialToCompetenceAndGetEvents(long credId, long compId, 
    		UserContextData context, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		//we should propagate all groups, event default
    		List<CredentialUserGroup> credGroups = getCredentialUserGroups(credId, true,
					UserGroupPrivilege.Edit, session);
    		for (CredentialUserGroup credGroup : credGroups) {
    			res.appendEvents(propagateUserGroupPrivilegeFromCredential(credGroup, compId, context,
						session).getEventQueue());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
    private Result<Void> propagateUserGroupPrivilegeFromCredential(CredentialUserGroup credUserGroup, long compId,
    		UserContextData context, Session session) throws DbConnectionException {
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

    		res.appendEvent(eventFactory.generateEventData(
    				EventType.RESOURCE_VISIBILITY_CHANGE, context, competence, null, null, null));
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
	@Override
	@Transactional(readOnly = false)
    public Result<Void> propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(long credId, 
    		long deliveryId, UserContextData context, Session session) throws DbConnectionException {
    	try {
    		Result<Void> res = new Result<>();
    		//we should propagate all groups, event default
    		List<CredentialUserGroup> credGroups = getCredentialUserGroups(credId, true, UserGroupPrivilege.Edit, 
    				session);
    		for (CredentialUserGroup credGroup : credGroups) {
    			res.appendEvents(propagateUserGroupPrivilegeFromCredentialToDelivery(credGroup, deliveryId, context, session)
    					.getEventQueue());
    		}
    		return res;
    	} catch(Exception e) {
    		e.printStackTrace();
    		logger.error(e);
    		throw new DbConnectionException("Error saving user privileges");
    	}
    }
	
    private Result<Void> propagateUserGroupPrivilegeFromCredentialToDelivery(CredentialUserGroup credUserGroup,
    		long deliveryId, UserContextData context, Session session) throws DbConnectionException {
		Result<Void> res = new Result<>();
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
			Credential1 delivery = new Credential1();
			delivery.setId(deliveryId);
			res.appendEvent(eventFactory.generateEventData(
					EventType.RESOURCE_VISIBILITY_CHANGE, context, delivery, null, null, null));

			session.flush();
		} catch (ConstraintViolationException e) {
			/**
			 * Constraint violation related to unique credId-userGroup-privilege can occur if delivery is created
			 * immediately after the credential has been created. In this case, there is a race condition for
			 * USER_GROUP_ADDED_TO_RESOURCE event (fired when a credential is created) that is trying to propagate
			 * Edit privilege to user, and this privilege has already been added when the delivery is created.
			 */
			logger.info("User group " + credUserGroup.getUserGroup().getId() + " already has " + credUserGroup.getPrivilege() + " privilege in delivery " + deliveryId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error saving user privileges");
		}
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
    public List<Long> getIdsOfUserGroupsAddedToCredential(long credId, boolean returnDefaultGroups, 
    		UserGroupPrivilege privilege, Session session) throws DbConnectionException {
		try {
    		StringBuilder query = new StringBuilder (
    					   "SELECT ug.id " +
						   "FROM CredentialUserGroup credGroup " +
    					   "INNER JOIN credGroup.userGroup ug " +
    					   "WHERE credGroup.credential.id = :credId " +
							   "AND ug.deleted IS FALSE ");
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
    		throw new DbConnectionException("Error retrieving user groups");
    	}
    }

    @Transactional
    @Override
    public Result<Void> addLearnPrivilegeToCredentialCompetencesAndGetEvents(long credId, long userId,
																			 UserContextData context,
																			 Session session) {
		Result<Void> res = new Result<>();
		try {
			List<Long> compIds = credManager.getIdsOfAllCompetencesInACredential(credId, session);
			for (long compId : compIds) {
				Result<CompetenceUserGroup> compUserGroupRes = getOrCreateDefaultCompetenceUserGroup(
						compId, UserGroupPrivilege.Learn, context, session);
				res.appendEvents(compUserGroupRes.getEventQueue());
				saveNewUserToCompetenceGroup(userId, compUserGroupRes.getResult(), session);

				Competence1 comp = new Competence1();
				comp.setId(compId);
				res.appendEvent(eventFactory.generateEventData(EventType.RESOURCE_VISIBILITY_CHANGE, context, comp, null, null,null));
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error creating user privileges");
		}
	}

	@Override
	@Transactional
	public Result<Void> createCredentialUserGroupAndSaveNewUser(long userId, long credId, UserGroupPrivilege privilege,
														boolean isDefault, UserContextData context)
			throws DbConnectionException {
		try {
			Result<CredentialUserGroup> res = createNewCredentialUserGroup(
					0, isDefault, credId, privilege, context);
			saveNewUserToCredentialGroup(userId, res.getResult());
			return Result.of(res.getEventQueue());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving user privilege");
		}
	}

	@Override
	@Transactional
	public Result<Void> createCompetenceUserGroupAndSaveNewUser(long userId, long compId, UserGroupPrivilege privilege,
																boolean isDefault, UserContextData context)
			throws DbConnectionException {
		try {
			Result<CompetenceUserGroup> res = createNewCompetenceUserGroup(
					0, isDefault, compId, privilege, context);
			saveNewUserToCompetenceGroup(userId, res.getResult());
			return Result.of(res.getEventQueue());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving user privilege");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<UserData> getPaginatedGroupUsers(long groupId, int limit, int offset)
			throws DbConnectionException {
		try {
			PaginatedResult<UserData> pRes = new PaginatedResult<>();
			pRes.setFoundNodes(getGroupUsers(groupId, limit, offset));
			pRes.setHitsNumber(countGroupUsers(groupId));

			return pRes;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error retrieving user groups");
		}
	}

	private long countGroupUsers(long groupId) {
		String query =
				"SELECT COUNT(u) " +
				"FROM UserGroupUser ugu " +
				"INNER JOIN ugu.user u " +
				"WHERE ugu.group.id = :groupId";

		return (long) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.uniqueResult();
	}

	private List<UserData> getGroupUsers(long groupId, int limit, int offset) {
		String query =
				"SELECT u " +
				"FROM UserGroupUser ugu " +
				"INNER JOIN ugu.user u " +
				"WHERE ugu.group.id = :groupId " +
				"ORDER BY u.lastname ASC, u.name ASC";

		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.list();

		List<UserData> res = new ArrayList<>();
		if(result != null) {
			for (User u : result) {
				res.add(new UserData(u));
			}
		}

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<UserData> getPaginatedGroupInstructors(long groupId, int limit, int offset) {
		try {
			PaginatedResult<UserData> res = new PaginatedResult<>();
			res.setHitsNumber(countGroupInstructors(groupId));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getGroupInstructors(groupId, limit, offset));
			}

			return res;
		} catch(Exception e) {
			throw new DbConnectionException("Error retrieving user group instructors", e);
		}
	}

	private long countGroupInstructors(long groupId) {
		String query =
				"SELECT COUNT(ugi) " +
				"FROM UserGroupInstructor ugi " +
				"WHERE ugi.group.id = :groupId";

		return (long) persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.uniqueResult();
	}

	private List<UserData> getGroupInstructors(long groupId, int limit, int offset) {
		String query =
				"SELECT u " +
				"FROM UserGroupInstructor ugi " +
				"INNER JOIN ugi.user u " +
				"WHERE ugi.group.id = :groupId " +
				"ORDER BY u.lastname ASC, u.name ASC";

		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager()
				.createQuery(query)
				.setLong("groupId", groupId)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.list();

		List<UserData> res = new ArrayList<>();
		if (result != null) {
			for (User u : result) {
				res.add(new UserData(u));
			}
		}

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public TitleData getUserGroupUnitAndOrganizationTitle(long organizationId, long unitId, long groupId)
			throws DbConnectionException {
		try {
			String q = "SELECT g.name, unit.title, org.title " +
					"FROM UserGroup g " +
					"INNER JOIN g.unit unit " +
						"WITH unit.id = :unitId " +
					"INNER JOIN unit.organization org " +
						"WITH org.id = :orgId " +
					"WHERE g.id = :groupId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(q)
					.setLong("groupId", groupId)
					.setLong("unitId", unitId)
					.setLong("orgId", organizationId)
					.uniqueResult();

			return res != null
					? TitleData.ofOrganizationUnitAndUserGroupTitle(
							(String) res[2], (String) res[1], (String) res[0])
					: null;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving user group data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getUserGroupIds(long userId, boolean returnDefaultGroupIds, Session session)
			throws DbConnectionException {
		try {
			String q =
					"SELECT g.id " +
					"FROM UserGroupUser ugu " +
					"INNER JOIN ugu.group g " +
					"WHERE ugu.user.id = :userId " +
						"AND g.deleted IS FALSE ";

			if (!returnDefaultGroupIds) {
				q += "AND g.defaultGroup IS FALSE ";
			}

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
					.createQuery(q)
					.setLong("userId", userId)
					.list();

			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving user group ids");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long countCredentialUserGroups(long credId, UserGroupPrivilege privilege) {
		try {
			StringBuilder query = new StringBuilder (
					"SELECT COUNT(credGroup) " +
							"FROM CredentialUserGroup credGroup " +
							"INNER JOIN credGroup.userGroup userGroup " +
							"WHERE credGroup.credential.id = :credId " +
							"AND userGroup.deleted IS FALSE " +
							"AND userGroup.defaultGroup = :defaultGroup ");
			if (privilege != null) {
				query.append("AND credGroup.privilege = :priv ");
			}
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setBoolean("defaultGroup", false);

			if (privilege != null) {
				q.setParameter("priv", privilege);
			}

			return (long) q.uniqueResult();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential groups");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getCredentialUserGroupsNames(long credId, UserGroupPrivilege privilege, int limit) {
		try {
			StringBuilder query = new StringBuilder (
					"SELECT userGroup.name " +
					   "FROM CredentialUserGroup credGroup " +
					   "INNER JOIN credGroup.userGroup userGroup " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND userGroup.deleted IS FALSE " +
					   "AND userGroup.defaultGroup = :defaultGroup ");
			if (privilege != null) {
				query.append("AND credGroup.privilege = :priv ");
			}

			query.append("ORDER BY userGroup.title");

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setBoolean("defaultGroup", false);

			if (privilege != null) {
				q.setParameter("priv", privilege);
			}

			if (limit > 0) {
				q.setMaxResults(limit);
			}

			return (List<String>) q.list();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential groups");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long countCredentialVisibilityUsers(long credId, UserGroupPrivilege privilege) {
		try {
			StringBuilder query = new StringBuilder(
					"SELECT COUNT(distinct user) FROM CredentialUserGroup credGroup " +
					"INNER JOIN credGroup.userGroup userGroup " +
					"INNER JOIN userGroup.users userGroupUser " +
					"INNER JOIN userGroupUser.user user " +
					"WHERE credGroup.credential.id = :credId " +
					"AND userGroup.defaultGroup = :defaultGroup " +
					"AND userGroup.deleted IS FALSE ");

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

			return (long) q.uniqueResult();
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential users");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getCredentialVisibilityUsersNames(long credId, UserGroupPrivilege privilege, int limit) {
		try {
			StringBuilder query = new StringBuilder(
					"SELECT distinct user FROM CredentialUserGroup credGroup " +
					   "INNER JOIN credGroup.userGroup userGroup " +
					   "INNER JOIN userGroup.users userGroupUser " +
					   "INNER JOIN userGroupUser.user user " +
					   "WHERE credGroup.credential.id = :credId " +
					   "AND userGroup.defaultGroup = :defaultGroup " +
					   "AND userGroup.deleted IS FALSE ");

			if (privilege != null) {
				query.append("AND credGroup.privilege = :priv ");
			}

			query.append("ORDER BY user.name, user.lastname");

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setBoolean("defaultGroup", true);

			if (privilege != null) {
				q.setString("priv", privilege.name());
			}

			if (limit > 0) {
				q.setMaxResults(limit);
			}

			List<User> users = (List) q.list();
			return users.stream().map(u -> u.getFullName()).collect(Collectors.toList());
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving credential users");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfUserGroupsWhereUserIsInstructor(long userId) {
		try {
			String q =
					"SELECT g.id " +
					"FROM UserGroupInstructor gi " +
					"INNER JOIN gi.group g " +
					"WHERE gi.user.id = :userId " +
					"AND g.deleted IS FALSE ";

			return (List<Long>) persistence.currentManager()
					.createQuery(q)
					.setLong("userId", userId)
					.list();
		} catch (Exception e) {
			throw new DbConnectionException("Error retrieving user group ids", e);
		}
	}

}
