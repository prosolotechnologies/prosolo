package org.prosolo.services.user;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.TitleData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.UserGroupData;

import java.util.List;

public interface UserGroupManager extends AbstractManager {

	List<UserGroup> getAllGroups(long orgId, boolean returnDefaultGroups, Session session) throws DbConnectionException;

	UserGroupData getGroup(long groupgId) throws DbConnectionException;

	List<UserGroupData> searchGroups(long unitId, String searchTerm, int limit, int page) throws DbConnectionException;

	long countGroups(long unitId, String searchTerm) throws DbConnectionException;

	UserGroup saveNewGroup(long unitId, String name, boolean isDefault, UserContextData context)
			throws DbConnectionException;

	UserGroup updateGroupName(long groupId, String newName, UserContextData context)
			throws DbConnectionException;

	UserGroup updateJoinUrl(long id, boolean joinUrlActive, String joinUrlPassword, UserContextData context);

	void deleteUserGroup(long id, UserContextData context) throws DbConnectionException;

	Result<Void> addUserToTheGroupAndGetEvents(long groupId, long userId, UserContextData context)
			throws DbConnectionException;

	void addUserToTheGroup(long groupId, long userId, UserContextData context)
			throws DbConnectionException;

	void removeUserFromTheGroup(long groupId, long userId, UserContextData context) throws DbConnectionException;

	Result<Void> removeUserFromTheGroupAndGetEvents(long groupId, long userId, UserContextData context) throws DbConnectionException;

	Result<Void> addUserToGroups(long userId, List<Long> groupIds) throws DbConnectionException;

	Result<Void> removeUserFromGroups(long userId, List<Long> groupIds, UserContextData context) throws DbConnectionException;

	void updateUserParticipationInGroups(long userId, List<Long> groupsToRemoveUserFrom,
										 List<Long> groupsToAddUserTo, UserContextData context) throws DbConnectionException;

	Result<Void> updateUserParticipationInGroupsAndGetEvents(long userId, List<Long> groupsToRemoveUserFrom,
															 List<Long> groupsToAddUserTo, UserContextData context) throws DbConnectionException;

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
											  List<ResourceVisibilityMember> users, UserContextData context) throws DbConnectionException;

	List<CredentialUserGroup> getAllCredentialUserGroups(long credId, Session session)
			throws DbConnectionException;

	List<ResourceVisibilityMember> getCompetenceVisibilityGroups(long compId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	List<ResourceVisibilityMember> getCompetenceVisibilityUsers(long compId, UserGroupPrivilege privilege)
			throws DbConnectionException;

	Result<Void> saveCompetenceUsersAndGroups(long compId, List<ResourceVisibilityMember> groups,
											  List<ResourceVisibilityMember> users, UserContextData context)
			throws DbConnectionException;

	boolean isUserInADefaultCredentialGroup(long userId, long credId) throws DbConnectionException;

	/**
	 * This method removes privilege for user group specified by {@code userGroupId} id from all competencies
	 * and credential deliveries for a credential given by {@code credId} and returns event data for all events
	 * that should be generated.
	 *
	 * @param credId
	 * @param userGroupId
	 * @param context
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> removeUserGroupPrivilegePropagatedFromCredentialAndGetEvents(long credId, long userGroupId,
																			  UserContextData context, Session session)
			throws DbConnectionException;

	/**
	 * This method removes EDIT privilege for all user groups that have EDIT privilege in a credential given by {@code credId} id
	 * in a competency given by {@code compId} and returns event data for all events that should be generated
	 *
	 * @param compId
	 * @param credId
	 * @param context
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> removeUserGroupPrivilegesPropagatedFromCredentialAndGetEvents(long compId, long credId,
																			   UserContextData context, Session session)
			throws DbConnectionException;

	/**
	 * This method propagates privilege for user group from a credential to all competencies
	 * that are part of that credential and all credential deliveries and returns event data
	 * for all events that should be generated
	 *
	 * @param credUserGroupId - id of a CredentialUserGroup instance
	 * @param context
	 * @param session
	 * @return
	 * @throws DbConnectionException
	 */
	Result<Void> propagateUserGroupPrivilegeFromCredentialAndGetEvents(long credUserGroupId, UserContextData context,
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
																					UserContextData context, Session session)
			throws DbConnectionException;

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
																					  long deliveryId,
																					  UserContextData context,
																					  Session session) throws DbConnectionException;

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
															  UserContextData context) throws DbConnectionException;



	Result<Void> removeUserFromDefaultCredentialGroupAndGetEvents(long userId, long credId,
																  UserGroupPrivilege privilege,
																  UserContextData context) throws DbConnectionException;

	Result<Void> saveUserToDefaultCompetenceGroupAndGetEvents(long userId, long compId,
															  UserGroupPrivilege privilege,
															  UserContextData context) throws DbConnectionException;

	Result<Void> removeUserFromDefaultCompetenceGroupAndGetEvents(long userId, long compId,
																  UserGroupPrivilege privilege,
																  UserContextData context) throws DbConnectionException;

	Result<Void> addLearnPrivilegeToCredentialCompetencesAndGetEvents(long credId, long userId,
																	  UserContextData context,
																	  Session session);

	Result<Void> createCredentialUserGroupAndSaveNewUser(long userId, long credId, UserGroupPrivilege privilege,
														 boolean isDefault, UserContextData context)
			throws DbConnectionException;

	Result<Void> createCompetenceUserGroupAndSaveNewUser(long userId, long compId, UserGroupPrivilege privilege,
														 boolean isDefault, UserContextData context)
			throws DbConnectionException;

	UserGroupData getUserCountAndCanBeDeletedGroupData(long groupId) throws DbConnectionException;

	PaginatedResult<UserData> getPaginatedGroupUsers(long groupId, int limit, int offset)
			throws DbConnectionException;

	TitleData getUserGroupUnitAndOrganizationTitle(long organizationId, long unitId, long groupId)
			throws DbConnectionException;

    List<Long> getUserGroupIds(long userId, boolean returnDefaultGroupIds, Session session)
            throws DbConnectionException;

	/**
	 *
	 * @param credId
	 * @param privilege
	 * @return
	 * @throws DbConnectionException
	 */
	long countCredentialUserGroups(long credId, UserGroupPrivilege privilege);

	List<String> getCredentialUserGroupsNames(long credId, UserGroupPrivilege privilege, int limit);

	/**
	 *
	 * @param credId
	 * @param privilege
	 * @return
	 * @throws DbConnectionException
	 */
	long countCredentialVisibilityUsers(long credId, UserGroupPrivilege privilege);

	List<String> getCredentialVisibilityUsersNames(long credId, UserGroupPrivilege privilege, int limit);

	Result<UserGroup> saveNewGroupAndGetEvents(long unitId, String name, boolean isDefault, UserContextData context) throws DbConnectionException;

	Result<UserGroup> updateGroupNameAndGetEvents(long groupId, String newName, UserContextData context) throws DbConnectionException;

}
