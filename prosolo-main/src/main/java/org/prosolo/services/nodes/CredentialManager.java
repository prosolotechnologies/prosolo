package org.prosolo.services.nodes;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.services.common.exception.CompetenceEmptyException;
import org.prosolo.services.common.exception.CredentialEmptyException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.LearningResourceReturnResultType;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;

public interface CredentialManager extends AbstractManager {

	Credential1 saveNewCredential(CredentialData data, long creatorId, LearningContextData context) 
			throws DbConnectionException;
	
	/**
	 * Deletes credential by setting deleted flag to true on original credential and 
	 * deleting draft version of a credential from database if exists.
	 * 
	 * IMPORTANT! Id of original credential should always be passed and not id of a
	 * draft version.
	 * @param originalCredId
	 * @param data
	 * @param user
	 * @return
	 * @throws DbConnectionException
	 */
	Credential1 deleteCredential(long originalCredId, CredentialData data, long userId) throws DbConnectionException;
	
	/**
	 * Returns user target credential data if user is enrolled in a credential, or credential data 
	 * if that is not the case.
	 * @param credentialId
	 * @param userId
	 * @throws DbConnectionException
	 */
	CredentialData getFullTargetCredentialOrCredentialData(long credentialId, long userId)
			throws DbConnectionException;
	
	/**
	 * Returns credential data with specified id. 
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER is passed for {@code returnType}
	 * parameter credential will be returned even if it is first time draft if creator of credential
	 * is user specified by {@code userId}.
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER is passed for {@code returnType}
	 * parameter credential will be returned even if it is first time draft if credential is created by
	 * university.
	 * @param credentialId
	 * @param loadCreatorData
	 * @param loadCompetences
	 * @param userId
	 * @param returnType
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialData(long credentialId, boolean loadCreatorData, boolean loadCompetences, 
			long userId, LearningResourceReturnResultType returnType) throws DbConnectionException;
	/**
	 * Returns credential with specified id. If credential is first time draft, it is only returned if
	 * creator of credential is user specified by {@code userId}
	 * @param credentialId
	 * @param loadCreatorData
	 * @param loadCompetences
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialDataForUser(long credentialId, boolean loadCreatorData,
			boolean loadCompetences, long userId) throws DbConnectionException;
	
	/**
	 * Returns credential with specified id. If credential is first time draft, it is only returned if
	 * credential is created by university
	 * @param credentialId
	 * @param loadCreatorData
	 * @param loadCompetences
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCredentialDataForManager(long credentialId, boolean loadCreatorData,
			boolean loadCompetences) throws DbConnectionException;
	
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
	
	/** Returns credential data for edit. If there is a draft version for a credential
	 *  that version data will be returned
	 *  
	 *  @param credentialId id of a credential
	 *  @param creatorId id of a user that will get credential data so 
	 *  we can check if he is a creator of a credential and he can 
	 *  edit it
	 *  @param loadCompetences if true credential competences data will be 
	 *  loaded too
	 */
	CredentialData getCredentialDataForEdit(long credentialId, long creatorId, boolean loadCompetences) 
			throws DbConnectionException;
	
	Credential1 updateCredential(long originalCredId, CredentialData data, long makerId, Role role,
			LearningContextData context) 
			throws DbConnectionException, CredentialEmptyException, CompetenceEmptyException;
	
	Result<Credential1> updateCredential(CredentialData data, long creatorId, Role role);
	
	CredentialData enrollInCredential(long credentialId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	/**
	 * Adds competence to credential, updates credential duration. If credential is published, draft version 
	 * for credential is created, competence is added to that draft version and original credential becomes draft. 
	 * If draft version for credential already exists, competence will be attached to existing draft version.
	 * 
	 * Returns data for events that should be generated when transaction is commited.
	 * 
	 * @param credId
	 * @param comp
	 * @param userId
	 * @throws DbConnectionException
	 */
	List<EventData> addCompetenceToCredential(long credId, Competence1 comp, long userId) 
			throws DbConnectionException;
	
	List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId) 
			throws DbConnectionException;

	void updateTargetCredentialsWithChangedData(long credentialId, CredentialChangeTracker changeTracker) 
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
	
	Credential1 getOriginalCredentialForDraft(long draftCredId) throws DbConnectionException;
	
	void bookmarkCredential(long credId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	CredentialBookmark bookmarkCredential(long credId, long userId) 
			throws DbConnectionException;
	
	void deleteCredentialBookmark(long credId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	long deleteCredentialBookmark(long credId, long userId) 
			throws DbConnectionException;
	
	Optional<Long> getDraftVersionIdIfExists(long credId) throws DbConnectionException;

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
	
	/**
	 * Target credential duration is updated by setting new duration value specified by
	 * {@code duration}
	 * @param id
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateTargetCredentialDuration(long id, long duration) throws DbConnectionException;

	void updateProgressForTargetCredentialWithCompetence(long targetCompId) throws DbConnectionException;
	
	void updateCredentialAndCompetenceProgressAndNextActivityToLearn(long credId, 
			long targetCompId, long targetActId, long userId, LearningContextData contextData) 
					throws DbConnectionException;
	
	String getCredentialTitle(long id) throws DbConnectionException;
	
	String getTargetCredentialTitle(long credId, long userId) throws DbConnectionException;
	
	String getCredentialDraftOrOriginalTitle(long id) throws DbConnectionException;
	
	/**
	 * Returns draft version of credential if exists, otherwise original version is returned.
	 * @param credentialId
	 * @param loadCreator
	 * @param loadCompetences
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getCurrentVersionOfCredentialForManager(long credentialId,
			boolean loadCreator, boolean loadCompetences) throws DbConnectionException;
	
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
	 * @param onlyForPublicPublicly - whether to load only credentials mark to be visible on public profile
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
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateHiddenTargetCredentialFromProfile(long id, boolean hiddenFromProfile) throws DbConnectionException;

	String getCredentialTitleForCredentialWithType(long id, LearningResourceType type) 
			throws DbConnectionException;
	
	TargetCredential1 getTargetCredential(long credentialId, long userId, 
			boolean loadCreator, boolean loadTags) throws DbConnectionException;
	
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
	
	boolean areStudentsManuallyAssignedToInstructor(long credId) throws DbConnectionException;

	List<Long> getTargetCredentialIdsForInstructor(long instructorId) throws DbConnectionException;
	
	long getUserIdForTargetCredential(long targetCredId) throws DbConnectionException;
	
	List<Long> getUserIdsForTargetCredentials(List<Long> targetCredIds) throws DbConnectionException;
	
	List<Long> getTargetCredentialIdsForUsers(List<Long> userIds, long credId) 
			throws DbConnectionException;
	
	boolean saveNewCredentialFeed(long credId, String feedLink) 
			throws DbConnectionException, EntityAlreadyExistsException;
	
	void removeFeed(long credId, long feedSourceId) throws DbConnectionException;
	
	List<Credential1> getAllCredentialsWithTheirDraftVersions(Session session) 
			throws DbConnectionException;
	
	CredentialData getTargetCredentialTitleAndNextCompAndActivityToLearn(long credId, long userId) 
			throws DbConnectionException;

	List<CredentialData> getNRecentlyLearnedInProgressCredentials(Long userid, int limit) 
			throws DbConnectionException;
	
	void updateTargetCredentialLastAction(long userId, long credentialId) 
			throws DbConnectionException;

	List<Long> getUserIdsForCredential(long credId) throws DbConnectionException;
	
	List<Long> getActiveUserIdsForCredential(long credId) throws DbConnectionException;
	
	CredentialData getTargetCredentialNextCompAndActivityToLearn(long credId, long userId) 
			throws DbConnectionException;
}
