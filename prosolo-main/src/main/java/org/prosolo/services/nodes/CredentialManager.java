package org.prosolo.services.nodes;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.nodes.data.resourceAccess.UserAccessSpecification;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;
import java.util.List;

public interface CredentialManager extends AbstractManager {

	/**
	 * Saves new credential.
	 * 
	 * This method should be used for saving original credential only and not for delivery.
	 * 
	 * @param data
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Credential1 saveNewCredential(CredentialData data, UserContextData context)
			throws DbConnectionException, EventException;

	Result<Credential1> saveNewCredentialAndGetEvents(CredentialData data, UserContextData context)
			throws DbConnectionException;
	
	void deleteDelivery(long deliveryId, UserContextData context) throws DbConnectionException, StaleDataException,
			DataIntegrityViolationException, EventException;
	
	Result<Void> deleteDeliveryAndGetEvents(long deliveryId, UserContextData context) throws DbConnectionException,
			DataIntegrityViolationException, StaleDataException;
	
	/**
	 * Returns user target credential data if user is enrolled in a credential, or credential data 
	 * if that is not the case.
	 * @param credentialId
	 * @param userId
	 * @throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException
	 */
	RestrictedAccessResult<CredentialData> getFullTargetCredentialOrCredentialData(long credentialId, long userId)
			throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	/**
	 * Returns credential data with specified id.
	 *
	 * If {@code req.getAccessMode()} equals {@code AccessMode.User} competencies that are returned if competencies
	 * should be loaded are user competencies with progress if exists. Otherwise credential competencies without user
	 * progress are returned
	 * 
	 * @param credentialId
	 * @param loadCreatorData
	 * @param loadCompetences
	 * @param userId
	 * @param req
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IllegalArgumentException
	 * @throws DbConnectionException
	 */
	RestrictedAccessResult<CredentialData> getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences, long userId, ResourceAccessRequirements req) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	RestrictedAccessResult<CredentialData> getCredentialDataForManagerView(long credentialId, 
			long userId) throws ResourceNotFoundException, DbConnectionException;
	
	RestrictedAccessResult<CredentialData> getCredentialForEdit(long credId, long userId) 
			throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	/**
	 * Returns Credential data for id: {@code credentialId} with user's progress
	 * for that credential if user is enrolled.
	 * @param credentialId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialDataWithProgressIfExists(long credentialId, long userId) 
			throws DbConnectionException;
	
	/**
	 * Returns basic credential data with info whether credential is bookmarked by user or not.
	 * @param credentialId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getBasicCredentialData(long credentialId, long userId) 
			throws DbConnectionException;
	
	/**
	 * Returns basic credential data with info whether credential is bookmarked by user or not, but only
	 * if credential is of type given by {@code type} parameter. Otherwise, null is returned.
	 * 
	 * @param credentialId
	 * @param userId
	 * @param type
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getBasicCredentialData(long credentialId, long userId, CredentialType type) 
			throws DbConnectionException;
	
//	/** Returns credential data for edit. If there is a draft version for a credential
//	 *  that version data will be returned
//	 *  
//	 *  @param credentialId id of a credential
//	 *  @param creatorId id of a user that will get credential data so
//	 *  we can check if he is a creator of a credential and he can
//	 *  edit it
//	 *  @param loadCompetences if true credential competences data will be 
//	 *  loaded too
//	 */
//	CredentialData getCredentialDataForEdit(long credentialId, long creatorId, boolean loadCompetences) 
//			throws DbConnectionException;
	
	Credential1 updateCredential(CredentialData data, UserContextData context)
			throws DbConnectionException, StaleDataException, IllegalDataStateException;
	
	Result<Credential1> updateCredentialData(CredentialData data, UserContextData context)
			throws StaleDataException, IllegalDataStateException;
	
	void enrollInCredential(long credentialId, UserContextData context)
			throws DbConnectionException, EventException;
	
	void enrollStudentsInCredential(long credId, long instructorId, List<Long> userIds,
									UserContextData context)
			throws DbConnectionException;
	
	Result<Void> enrollInCredentialAndGetEvents(long credentialId, long userId, 
			long instructorThatForcedEnrollId, UserContextData context) throws DbConnectionException;
	
	/**
	 * Adds competence to credential, updates credential duration. If credential is published, draft version 
	 * for credential is created, competence is added to that draft version and original credential becomes draft. 
	 * If draft version for credential already exists, competence will be attached to existing draft version.
	 * 
	 * Returns data for events that should be generated when transaction is commited.
	 * 
	 * @param credId
	 * @param comp
	 * @param context
	 * @throws DbConnectionException
	 */
	List<EventData> addCompetenceToCredential(long credId, Competence1 comp, UserContextData context)
			throws DbConnectionException;

	/**
	 * returns only published credentials
	 * @param compId
	 * @param type - if null credentials and deliveries both will be returned
	 * @return
	 * @throws DbConnectionException
	 */
	List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId, CredentialType type)
			throws DbConnectionException;

	List<Tag> getCredentialTags(long credentialId) 
			throws DbConnectionException;
	
	List<Tag> getCredentialTags(long credentialId, Session session) 
			throws DbConnectionException;
	
	List<Tag> getCredentialHashtags(long credentialId) 
			throws DbConnectionException;
	
	List<Tag> getCredentialHashtags(long credentialId, Session session) 
				throws DbConnectionException;

	List<CredentialBookmark> getBookmarkedByIds(long id) throws DbConnectionException;
	
	List<CredentialBookmark> getBookmarkedByIds(long credId, Session session) 
			throws DbConnectionException;
	
	void bookmarkCredential(long credId, UserContextData context)
			throws DbConnectionException;
	
	CredentialBookmark bookmarkCredential(long credId, long userId) 
			throws DbConnectionException;
	
	void deleteCredentialBookmark(long credId, UserContextData context)
			throws DbConnectionException;
	
	long deleteCredentialBookmark(long credId, long userId) 
			throws DbConnectionException;

	/**
	 * Duration for all credentials with competence specified by {@code compId} is updated
	 * by adding/subtracting {@code duration} value.
	 * @param compId
	 * @param duration
	 * @param op
	 * @throws DbConnectionException
	 */
	void updateDurationForCredentialsWithCompetence(long compId, long duration, Operation op)
			throws DbConnectionException;

	List<EventData> updateCredentialProgress(long targetCompId, UserContextData context)
			throws DbConnectionException;
	
	String getCredentialTitle(long id) throws DbConnectionException;
	
	String getCredentialTitle(long id, CredentialType type) throws DbConnectionException;

	String getTargetCredentialTitle(long userId) throws DbConnectionException;
	
	/**
	 * Returns draft version of credential if exists, otherwise original version is returned.
	 * @param credentialId
	 * @param loadCreator
	 * @param loadCompetences
	 * @return
	 * @throws DbConnectionException
	 */
//	CredentialData getCurrentVersionOfCredentialForManager(long credentialId,
//			boolean loadCreator, boolean loadCompetences) throws DbConnectionException;
	
	/**
	 * Method for getting all credentials (nevertheless the progress)
	 * 
	 * @param userId
	 * @param onlyForPublicPublicly - whether to load only credentials mark to be visible on public profile
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCredential1> getAllCredentials(long userId, boolean onlyForPublicPublicly) throws DbConnectionException;
	
	/**
	 * Method for getting all completed credentials (credentials that has progress == 100)
	 * 
	 * @param userId
	 * @param onlyPubliclyVisible - whether to load only credentials mark to be visible on public profile
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCredential1> getAllCompletedCredentials(long userId, boolean onlyPubliclyVisible) throws DbConnectionException;
	
	/**
	 * Method for getting all uncompleted credentials (credentials that has progress < 100)
	 * 
	 * @param userId
	 * @param onlyForPublicPublicly - whether to load only credentials mark to be visible on public profile
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCredential1> getAllInProgressCredentials(long userId, boolean onlyForPublicPublicly) throws DbConnectionException;

		
	/**
	 * Updated hidden_from_profile_field
	 * @param id
	 * @param hiddenFromProfile
	 * @throws DbConnectionException
	 */
	void updateHiddenTargetCredentialFromProfile(long id, boolean hiddenFromProfile) throws DbConnectionException;
	
	TargetCredential1 getTargetCredential(long credentialId, long userId, 
			boolean loadCreator, boolean loadTags, boolean loadInstructor) throws DbConnectionException;
	
	List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId) throws DbConnectionException;
	
	List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId, Session session) 
			throws DbConnectionException;
	
//	/**
//	 * Returns current version of credential for edit if edit mode - draft version if exists
//	 * but only if credential is university based, otherwise null is returned.
//	 * If view mode, again draft version is returned if exists and credential is university based, 
//	 * otherwise published version is returned.
//	 * @param credentialId
//	 * @param loadCreator
//	 * @param loadCompetences
//	 * @param mode
//	 * @return
//	 * @throws DbConnectionException
//	 */
//	CredentialData getCredentialForManager(long credentialId, boolean loadCreator,
//			boolean loadCompetences, Mode mode) throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForInstructor(long instructorId) throws DbConnectionException;
	
	long getUserIdForTargetCredential(long targetCredId) throws DbConnectionException;
	
	List<Long> getUserIdsForTargetCredentials(List<Long> targetCredIds) throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForUsers(List<Long> userIds, long credId) 
			throws DbConnectionException;
	
	boolean saveNewCredentialFeed(long credId, String feedLink) 
			throws DbConnectionException, EntityAlreadyExistsException;
	
	void removeFeed(long credId, long feedSourceId) throws DbConnectionException;

	List<CredentialData> getNRecentlyLearnedInProgressCredentials(Long userid, int limit, boolean loadOneMore) 
			throws DbConnectionException;
	
	void updateTargetCredentialLastAction(long userId, long credentialId) 
			throws DbConnectionException;

	List<Long> getUserIdsForCredential(long credId) throws DbConnectionException;
	
	List<Long> getActiveUserIdsForCredential(long credId) throws DbConnectionException;
	
	long getTargetCredentialNextCompToLearn(long credId, long userId) 
			throws DbConnectionException;
	
	long getNumberOfUsersLearningCredential(long credId) 
			throws DbConnectionException;
	
	List<StudentData> getCredentialStudentsData(long credId, int limit) 
			throws DbConnectionException;
	
	CredentialMembersSearchFilter[] getFiltersWithNumberOfStudentsBelongingToEachCategory(long credId) 
			throws DbConnectionException;
	
	List<Credential1> getAllCredentials(Session session) throws DbConnectionException;
	
	List<TargetCredential1> getTargetCredentialsForCredential(long credentialId, 
			boolean justUncompleted) throws DbConnectionException;
	
	void updateCredentialVisibility(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
    		UserContextData context) throws DbConnectionException, EventException;
	
	List<EventData> updateCredentialVisibilityAndGetEvents(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
    		UserContextData context) throws DbConnectionException;
	
	boolean isVisibleToAll(long credId) throws DbConnectionException;

	UserData chooseRandomPeer(long credId, long userId);
	
	
	/**
	 * Returns list of ids of all assessors that this particular user has asked
	 * for assessment for the credential with the given id
	 * 
	 * @param credentialId credential id
	 * @param userId user id
	 * @return list of ids
	 */
	List<Long> getAssessorIdsForUserAndCredential(long credentialId, long userId);
	
	/**
	 * Returns list of CompetenceData for given credentials.
	 * 
	 * @param credentialId
	 * @return list of simple data types from TargetCompetence
	 * @throws DbConnectionException
	 */
	List<CompetenceData1> getCompetencesForKeywordSearch(long credentialId) throws DbConnectionException;

	/**
	 * Returns tag titles for tags added to competences and activities that are part of the credential
	 * specified by {@code credentialId} id
	 * 
	 * @param credentialId

	 * @throws DbConnectionException
	 */
	List<String> getTagsFromCredentialCompetencesAndActivities(long credentialId) throws DbConnectionException;

	/**
	 * Returns list of ActivityData for given credentials.
	 * 
	 * @param credentialId
	 * @return list of simple data types from TargetActivity(title, id, duration and competenceId)
	 * @throws DbConnectionException
	 */
	List<ActivityData> getActivitiesForKeywordSearch(long credentialId) throws DbConnectionException;
	/**
	 * Returns list of ids of all users that currently do not have instructor assigned for credential
	 * with {@code credId} id, except users which ids are contained in {@code usersToExclude} list.
	 * 
	 * If you do not want to exclude any user, pass null or empty list for {@code usersToExclude} parameter.
	 *
	 * @param credId
	 * @param usersToExclude
	 * @return
	 * @throws DbConnectionException
	 */
	List<Long> getUnassignedCredentialMembersIds(long credId, List<Long> usersToExclude) 
			throws DbConnectionException;

	int getNumberOfTags(long credentialId) throws DbConnectionException;

	CredentialData getTargetCredentialData(long credentialId, long userId, boolean loadCompetences)
			throws DbConnectionException;
	
	LearningInfo getCredentialLearningInfo(long credId, long userId, boolean loadCompLearningInfo) 
			throws DbConnectionException;
	
	List<CredentialData> getActiveDeliveries(long credId) throws DbConnectionException;
	
	RestrictedAccessResult<List<CredentialData>> getCredentialDeliveriesWithAccessRights(long credId, 
			long userId) throws DbConnectionException;
	
	void archiveCredential(long credId, UserContextData context) throws DbConnectionException;
	
	void restoreArchivedCredential(long credId, UserContextData context) throws DbConnectionException;
	
	long countNumberOfCredentials(CredentialSearchFilterManager searchFilter, long userId, UserGroupPrivilege priv) 
			throws DbConnectionException, NullPointerException;
	
	List<CredentialData> searchCredentialsForManager(CredentialSearchFilterManager searchFilter, int limit, int page, 
			LearningResourceSortOption sortOption, long userId) throws DbConnectionException, NullPointerException;
	
	UserAccessSpecification getUserPrivilegesForCredential(long credId, long userId) throws DbConnectionException;
	
	ResourceAccessData getResourceAccessData(long credId, long userId, ResourceAccessRequirements req) 
			throws DbConnectionException;
	
	List<Long> getIdsOfAllCompetencesInACredential(long credId, Session session) throws DbConnectionException;
	
	Credential1 createCredentialDelivery(long credentialId, long start, long end, UserContextData context)
			throws DbConnectionException, IllegalDataStateException, EventException;
	
	Result<Credential1> createCredentialDeliveryAndGetEvents(long credentialId, Date start, Date end, 
			UserContextData context) throws DbConnectionException, IllegalDataStateException;
	
	List<Long> getIdsOfAllCredentialDeliveries(long credId, Session session) throws DbConnectionException;

	Result<Void> updateCredentialCreator(long newCreatorId, long oldCreatorId,
										 UserContextData context) throws DbConnectionException;

	ResourceCreator getCredentialCreator(long credId) throws DbConnectionException;

	List<Tag> getTagsForCredential(long credentialId) throws DbConnectionException;

	List<Tag> getHashtagsForCredential(long credentialId) throws DbConnectionException;

	List<CredentialData> getCredentialDeliveriesForUserWithInstructPrivilege(long userId)
			throws DbConnectionException;

	long getCredentialIdForDelivery(long deliveryId) throws DbConnectionException;

	List<Long> getDeliveryIdsForCredential(long credId) throws DbConnectionException;

	List<Long> getIdsOfUncompletedDeliveries(long userId) throws DbConnectionException;

	List<Long> getIdsOfDeliveriesUserIsLearning(long userId, UserLearningProgress progress)
			throws DbConnectionException;

	List<Long> getIdsOfDeliveriesUserIsLearningContainingCompetence(long userId, long compId)
			throws DbConnectionException;

}
