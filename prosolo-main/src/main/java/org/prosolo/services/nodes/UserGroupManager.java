package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.UserGroupData;

import java.util.List;

public interface UserGroupManager extends AbstractManager {

	List<UserGroup> getAllGroups(boolean returnDefaultGroups, Session session) throws DbConnectionException;

	UserGroupData getGroup(long groupgId) throws DbConnectionException;

	List<UserGroupData> searchGroups(long unitId, String searchTerm, int limit, int page) throws DbConnectionException;

	long countGroups(long unitId, String searchTerm) throws DbConnectionException;

	UserGroup saveNewGroup(long unitId, String name, boolean isDefault, long userId, LearningContextData context)
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
	 *
	 * @param credId
	 * @param privilege
	 * @return
	 * @throws DbConnectionException
	 */
	List<ResourceVisibilityMember> getCredentialVisibilityGroups(long credId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	/**
	 * Returns all users data from default user groups defined for credential.
	 *
	 * @param credId
	 * @param privilege
	 * @return
	 * @throws DbConnectionException
	 */
	List<ResourceVisibilityMember> getCredentialVisibilityUsers(long credId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	/**
	 * Saves all newly added credential user groups, updates groups if there were privilege changes,
	 * removes credential groups that should be removed from db. Also this method saves, updates and removes
	 * individual users inside credential default user groups.
	 *
	 * @param credId
	 * @param groups
	 * @param users
	 * @param actorId - actor that issued a request
	 * @param lcd
	 * @throws DbConnectionException
	 */
	Result<Void> saveCredentialUsersAndGroups(long credId, List<ResourceVisibilityMember> groups,
											  List<ResourceVisibilityMember> users, long actorId, LearningContextData lcd) throws DbConnectionException;

	List<CredentialUserGroup> getAllCredentialUserGroups(long credId, Session session)
			throws DbConnectionException;

	List<ResourceVisibilityMember> getCompetenceVisibilityGroups(long compId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	Result<Void> saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups,
											  List<ResourceVisibilityMember> users, long actorId,
											  LearningContextData lcd) throws DbConnectionException;

	boolean isUserInADefaultCredentialGroup(long userId, long credId) throws DbConnectionException;

	/**
	 * This method removes privilege for user group specified by {@code userGroupId} id from all competencies
	 * and credential deliveries for a credential given by {@code credId} and returns event data for all events
	 * that should be generated.
	 *
	 * @param credId
	 * @param userGroupId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> removeUserGroupPrivilegePropagatedFromCredentialAndGetEvents(long credId, long userGroupId,
																			  Session session) throws DbConnectionException;

	/**
	 * This method removes EDIT privilege for all user groups that have EDIT privilege in a credential given by {@code credId} id
	 * in a competency given by {@code compId} and returns event data for all events that should be generated
	 *
	 * @param compId
	 * @param credId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(long compId, long credId,
																			   Session session) throws DbConnectionException;

	/**
	 * This method propagates privilege for user group from a credential to all competencies
	 * that are part of that credential and all credential deliveries and returns event data
	 * for all events that should be generated
	 *
	 * @param credUserGroupId - id of a CredentialUserGroup instance
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> propagateUserGroupPrivilegeFromCredentialAndGetEvents(long credUserGroupId,
																	   Session session) throws DbConnectionException;

	/**
	 * This method propagates EDIT privileges for all user groups that have EDIT privilege in a credential given by {@code credId}
	 * to competency specified by {@code compId} and returns event data for all events that should be generated
	 *
	 * @param credId
	 * @param compId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> propagateUserGroupPrivilegesFromCredentialToCompetenceAndGetEvents(long credId, long compId,
																					Session session) throws DbConnectionException;

	/**
	 * Propagates edit privileges from credential specified by {@code credId} id to all credential deliveries
	 * and returns events that should be generated.
	 *
	 * @param credId
	 * @param deliveryId
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(long credId,
																					  long deliveryId, Session session) throws DbConnectionException;

	/**
	 * @param credId
	 * @param returnDefaultGroups
	 * @param privilege           - pass null if ids should be returned for groups with any privilege
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	List<Long> getIdsOfUserGroupsAddedToCredential(long credId, boolean returnDefaultGroups,
												   UserGroupPrivilege privilege, Session session) throws DbConnectionException;

	Result<Void> saveUserToDefaultCredentialGroupAndGetEvents(long userId, long credId, UserGroupPrivilege privilege,
															  long actorId, LearningContextData context) throws DbConnectionException;



	Result<Void> removeUserFromDefaultCredentialGroupAndGetEvents(long userId, long credId,
																  UserGroupPrivilege privilege, long actorId,
																  LearningContextData context) throws DbConnectionException;

	Result<Void> saveUserToDefaultCompetenceGroupAndGetEvents(long userId, long compId,
															  UserGroupPrivilege privilege, long actorId,
															  LearningContextData context) throws DbConnectionException;

	Result<Void> removeUserFromDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																  UserGroupPrivilege privilege, long actorId,
																  LearningContextData context) throws DbConnectionException;

	Result<Void> addLearnPrivilegeToCredentialCompetencesAndGetEvents(long credId, long userId,
																	  long actorId, LearningContextData context,
																	  Session session);

	Result<Void> createCredentialUserGroupAndSaveNewUser(long userId, long credId, UserGroupPrivilege privilege,
														 boolean isDefault, long actorId, LearningContextData context)
			throws DbConnectionException;

	Result<Void> createCompetenceUserGroupAndSaveNewUser(long userId, long compId, UserGroupPrivilege privilege,
														 boolean isDefault, long actorId, LearningContextData context)
			throws DbConnectionException;

	UserGroupData getUserCountAndCanBeDeletedGroupData(long groupId) throws DbConnectionException;

	PaginatedResult<UserData> getPaginatedGroupUsers(long groupId, int limit, int offset)
			throws DbConnectionException;

	TitleData getUserGroupUnitAndOrganizationTitle(long organizationId, long unitId, long groupId)
			throws DbConnectionException;

	List<Long> getUserGroupIds(long userId, boolean returnDefaultGroupIds)
			throws DbConnectionException;
}
