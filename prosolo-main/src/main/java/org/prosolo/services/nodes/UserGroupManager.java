package org.prosolo.services.nodes;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserGroupData;

public interface UserGroupManager extends AbstractManager {

	List<UserGroup> getAllGroups() throws DbConnectionException;

	UserGroupData getGroup(long groupgId) throws DbConnectionException;
	
	List<UserGroupData> searchGroups(String searchTerm, int limit, int page) throws DbConnectionException;
	
	long countGroups(String searchTerm) throws DbConnectionException;
	
	UserGroup saveNewGroup(String name, boolean isDefault, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	UserGroup updateGroupName(long groupId, String newName, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	UserGroup updateJoinUrl(long id, boolean joinUrlActive, String joinUrlPassword, long userId, LearningContextData lcd);
	
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
			List<Long> groupsToAddUserTo) throws DbConnectionException;
	
	long getNumberOfUsersInAGroup(long groupId) throws DbConnectionException;
	
	boolean isUserInGroup(long groupId, long userId) throws DbConnectionException;
	
	List<CredentialUserGroup> getCredentialUserGroups(long groupId) throws DbConnectionException;
	
	List<CredentialUserGroup> getAllCredentialUserGroups(long credId) throws DbConnectionException;
	
	List<CompetenceUserGroup> getAllCompetenceUserGroups(long compId) 
    		throws DbConnectionException;
	
	List<CompetenceUserGroup> getAllCompetenceUserGroups(long compId, Session session) 
    		throws DbConnectionException;
	
	List<CompetenceUserGroup> getCompetenceUserGroups(long groupId) throws DbConnectionException;
	
	/**
	 * Returns list of all non default user groups for specified credential
	 * @param credId
	 * @return
	 * @throws DbConnectionException
	 */
	List<ResourceVisibilityMember> getCredentialVisibilityGroups(long credId) 
    		throws DbConnectionException;
	
	/**
	 * Returns all users data from default user groups defined for credential.
	 * @param credId
	 * @return
	 * @throws DbConnectionException
	 */
	List<ResourceVisibilityMember> getCredentialVisibilityUsers(long credId) 
    		throws DbConnectionException;
	
	/**
	 * Saves all newly added credential user groups, updates groups if there were privilege changes,
	 * removes credential groups that should be removed from db. Also this method saves, updates and removes
	 * individual users inside credential default user groups.
	 * @param credId
	 * @param groups
	 * @param users
	 * @throws DbConnectionException
	 */
	void saveCredentialUsersAndGroups(long credId, List<ResourceVisibilityMember> groups, 
	    	List<ResourceVisibilityMember> users) throws DbConnectionException;
	
	List<CredentialUserGroup> getAllCredentialUserGroups(long credId, Session session) 
    		throws DbConnectionException;
	
	List<ResourceVisibilityMember> getCompetenceVisibilityGroups(long compId) 
    		throws DbConnectionException;
	
	List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId) 
    		throws DbConnectionException;
	
	void saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users) throws DbConnectionException;
	
	boolean isUserInADefaultCredentialGroup(long userId, long credId) throws DbConnectionException;
	
	/**
	 * Adds user to a default credential user group according to the privilege specified by
	 * {@code privilege} parameter.
	 * 
	 * If user is already a member of a DEFAULT credential group with ANY privilege, he is not
	 * added again.
	 * If there is no default credential user group, it is created.
	 * 
	 * @param userId
	 * @param credId
	 * @param privilege
	 * @throws DbConnectionException
	 */
	void addUserToADefaultCredentialGroupIfNotAlreadyMember(long userId, long credId,
    		UserGroupPrivilege privilege) throws DbConnectionException;

}
