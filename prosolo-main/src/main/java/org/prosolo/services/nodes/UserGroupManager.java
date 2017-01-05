package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UserGroupData;

public interface UserGroupManager extends AbstractManager {

	List<UserGroup> getAllGroups() throws DbConnectionException;
	
	List<UserGroupData> searchGroups(String searchTerm, int limit, int page) throws DbConnectionException;
	
	long countGroups(String searchTerm) throws DbConnectionException;
	
	UserGroup saveNewGroup(String name, boolean isDefault, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	UserGroup updateGroupName(long groupId, String newName, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	void deleteUserGroup(long id, long userId, LearningContextData context) throws DbConnectionException;
	
	void addUserToTheGroup(long groupId, long userId) throws DbConnectionException;
	
	void removeUserFromTheGroup(long groupId, long userId) throws DbConnectionException;
	
	void addUsersToTheGroup(long groupId, List<Long> userIds) throws DbConnectionException;
	
	void removeUsersFromTheGroup(long groupId, List<Long> userIds) throws DbConnectionException;
	
	void updateGroupUsers(long groupId, List<Long> usersToAdd, List<Long> usersToRemove) 
			throws DbConnectionException;
	
	void addUserToGroups(long userId, List<Long> groupIds) throws DbConnectionException;
	
	void removeUserFromGroups(long userId, List<Long> groupIds) throws DbConnectionException;
	
	void updateUserParticipationInGroups(long userId, List<Long> groupsToRemoveUserFrom, 
			List<Long> groupsToAddUserTo, long actorId, LearningContextData context) throws DbConnectionException;
	
	long getNumberOfUsersInAGroup(long groupId) throws DbConnectionException;
	
	boolean isUserInGroup(long groupId, long userId) throws DbConnectionException;

}
