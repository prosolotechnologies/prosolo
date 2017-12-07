package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.resourceAccess.*;
import org.prosolo.web.achievements.data.TargetCompetenceData;
import org.w3c.dom.events.EventException;

import java.util.List;

public interface Competence1Manager {

	/**
	 * Saves new competence to the database. If {@code credentialId} > 0
	 * it will add new competence to credential with that id. If
	 * you don't want to add competence to credential, just pass
	 * 0 for {@code credentialId}
	 * @param data
	 * @param credentialId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 saveNewCompetence(CompetenceData1 data, long credentialId,
			UserContextData context) throws DbConnectionException, IllegalDataStateException;

	Result<Competence1> saveNewCompetenceAndGetEvents(CompetenceData1 data, long credentialId,
													  UserContextData context) throws DbConnectionException,
			IllegalDataStateException;
	
	/**
	 * Updates competence.
	 * 
	 * DB Locks:
	 * exclusive lock on a competence being updated
	 * 
	 * @param data
	 * @param userId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 * @throws IllegalDataStateException
	 * @throws StaleDataException
	 */
	Competence1 updateCompetence(CompetenceData1 data, UserContextData context)
			throws DbConnectionException, IllegalDataStateException, StaleDataException;
	
	/**
	 * Updates competence.
	 * 
	 * DB Locks:
	 * exclusive lock on a competence being updated
	 * 
	 * @param data
	 * @param userId
	 * @return
	 * @throws StaleDataException
	 * @throws IllegalDataStateException
	 */
	Competence1 updateCompetenceData(CompetenceData1 data, long userId) throws StaleDataException, 
			IllegalDataStateException;
	
	List<CompetenceData1> getCompetencesForCredential(long credId, long userId, boolean loadCreator, boolean loadTags,
		  boolean loadActivities) throws DbConnectionException;
	
	
	/**
	 * Returns competence data with access rights info for user specified by {@code userId} id.
	 * 
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param userId
	 * @param req
	 * @param shouldTrackChanges
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IllegalArgumentException
	 * @throws DbConnectionException
	 */
	RestrictedAccessResult<CompetenceData1> getCompetenceDataWithAccessRightsInfo(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, long userId, ResourceAccessRequirements req,
			boolean shouldTrackChanges) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	/**
	 * 
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param shouldTrackChanges
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IllegalArgumentException
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean shouldTrackChanges) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;

	List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished)
					throws DbConnectionException;
	
	List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean includeNotPublished) 
					throws DbConnectionException;
	
	/**
	 * Returns list of credential competencies (together with competencies data loaded) for credential with {@code credentialId} id.
	 * 
	 * @param credentialId
	 * @param loadCreator
	 * @param loadTags
	 * @param includeNotPublished
	 * @param usePessimisticLock - load credential competencies with pessimistic lock
	 * @return
	 * @throws DbConnectionException
	 */
	List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean includeNotPublished, boolean usePessimisticLock) throws DbConnectionException;
	
//	CompetenceData1 getTargetCompetenceData(long targetCompId, boolean loadActivities, 
//			boolean loadCredentialTitle) throws DbConnectionException;
	
	List<Tag> getCompetenceTags(long compId) 
			throws DbConnectionException;
	
	List<Tag> getCompetenceTags(long compId, Session session) 
			throws DbConnectionException;

//	/**
//	 * Sets published to true for all competences from the list that do not have
//	 * draft version
//	 * @param compIds
//	 * @throws DbConnectionException
//	 */
//	void publishDraftCompetencesWithoutDraftVersion(List<Long> compIds) throws DbConnectionException;
	
	/**
	 * Call this method when you want to add activity to competence.
	 * 
	 * DB Locks:
	 * This method uses exclusive lock on a competence1 record with id {@code compId} for the duration of a transaction.
	 * 
	 * Returns data for event that should be generated after transaction is commited.
	 * 
	 * 
	 * @param compId
	 * @param act
	 * @param context
	 * @throws DbConnectionException, IllegalDataStateException
	 */
	EventData addActivityToCompetence(long compId, Activity1 act, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	/**
	 * Duration for competences with activity specified by {@code actId} are updated by adding/subtracting {@code duration} value.
	 * One or two competences will be updated - draft and/or original version of one competence actually.
	 * If original version of competence is updated, duration for all credentials that include
	 * this competence is also updated.
	 * @param actId
	 * @param duration
	 * @param op
	 * @throws DbConnectionException
	 */
	void updateDurationForCompetenceWithActivity(long actId, long duration, Operation op) throws DbConnectionException;
	
	String getCompetenceTitle(long id) throws DbConnectionException;
	
	/**
	 * Returns full target competence data if user is enrolled, otherwise it returns
	 * full competence data.
	 * @param credId
	 * @param compId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	RestrictedAccessResult<CompetenceData1> getFullTargetCompetenceOrCompetenceData(long credId, long compId,
			long userId) throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException;
	
	List<Competence1> getAllCompetences(long orgId, Session session)
			throws DbConnectionException;
	
//	/**
//	 * Returns current version of competence for edit if edit mode - draft version if exists
//	 * but only if competence is university based, otherwise null is returned.
//	 * If view mode, again draft version is returned if exists and competence is university based, 
//	 * otherwise published version is returned.
//	 * @param competenceId
//	 * @param loadCreator
//	 * @param loadActivities
//	 * @param mode
//	 * @return
//	 * @throws DbConnectionException
//	 */
//	CompetenceData1 getCompetenceForManager(long competenceId, boolean loadCreator, 
//			boolean loadActivities, Mode mode) throws DbConnectionException;
	
	/**
	 * Returns user access specification object for a user with {@code userId} id and competence with {@code compId} id.
	 * 
	 * @param compId
	 * @param userId
	 * @return {@link UserGroupPrivilege}
	 */
	UserAccessSpecification getUserPrivilegesForCompetence(long compId, long userId) 
			throws DbConnectionException;
	
	/**
	 * 
	 * @param compId
	 * @param userId
	 * @param req
	 * @return
	 * @throws DbConnectionException
	 */
	ResourceAccessData getResourceAccessData(long compId, long userId, ResourceAccessRequirements req) 
			throws DbConnectionException;
	
	boolean isVisibleToAll(long compId) throws DbConnectionException;

	void updateCompetenceVisibility(long compId, List<ResourceVisibilityMember> groups,
							   List<ResourceVisibilityMember> users, boolean visibleToAll,
							   boolean visibleToAllChanged, UserContextData context)
			throws DbConnectionException;

	EventQueue updateCompetenceVisibilityAndGetEvents(long compId, List<ResourceVisibilityMember> groups,
													  List<ResourceVisibilityMember> users, boolean visibleToAll,
													  boolean visibleToAllChanged, UserContextData context)
			throws DbConnectionException;
	
	/**
	 * Returns competence data without tags and activities and with user progress set if exists 
	 * and bookmark from current user.
	 *
	 * @param compId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceDataWithProgressIfExists(long compId, long userId) 
			throws DbConnectionException;
	
	/**
	 * Returns competence data without tags and activities and with bookmark from current user.
	 * 
	 * @param compId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getBasicCompetenceData(long compId, long userId) 
			throws DbConnectionException;
	
	void bookmarkCompetence(long compId, UserContextData context)
			throws DbConnectionException;

	Result<Void> bookmarkCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException;
	
	void deleteCompetenceBookmark(long compId, UserContextData context)
			throws DbConnectionException;

	Result<Void> deleteCompetenceBookmarkAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException;
	
	List<CompetenceBookmark> getBookmarkedByIds(long compId, Session session) 
			throws DbConnectionException;
	
	List<CompetenceBookmark> getBookmarkedByIds(long compId) throws DbConnectionException;
	
	List<TargetCompetence1> getTargetCompetencesForCompetence(long compId, 
			boolean justUncompleted) throws DbConnectionException;
	
	void enrollInCompetence(long compId, long userId, UserContextData context)
			throws DbConnectionException;

	Result<TargetCompetence1> enrollInCompetenceAndGetEvents(long compId, long userId, UserContextData context)
			throws DbConnectionException;
	
	CompetenceData1 enrollInCompetenceAndGetCompetenceData(long compId, long userId,
														   UserContextData context)
			throws DbConnectionException;

	Result<CompetenceData1> enrollInCompetenceGetCompetenceDataAndGetEvents(long compId, long userId, UserContextData context)
			throws DbConnectionException;
	
	long countNumberOfStudentsLearningCompetence(long compId) throws DbConnectionException;
	
	void archiveCompetence(long compId, UserContextData context) throws DbConnectionException;

	Result<Void> archiveCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException;
	
	/**
	 * Returns number of competences for which user specified with {@code userId} has privilege specified by {@code priv}
	 * @param searchFilter
	 * @param userId
	 * @param priv
	 * @return
	 * @throws DbConnectionException
	 * @throws NullPointerException
	 */
	long countNumberOfCompetences(CompetenceSearchFilter searchFilter, long userId, UserGroupPrivilege priv) 
			throws DbConnectionException, NullPointerException;
	
	List<CompetenceData1> searchCompetencesForManager(CompetenceSearchFilter searchFilter, int limit, int page, 
			LearningResourceSortOption sortOption, long userId) throws DbConnectionException, NullPointerException;
	
	long duplicateCompetence(long compId, UserContextData context) throws DbConnectionException;

	Result<Competence1> duplicateCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException;

	Result<Competence1> getOrCreateCompetenceInLearningStageAndGetEvents(long basedOnCompId, long learningStageId, UserContextData context)
			throws DbConnectionException;
	
	String getCompetenceTitleForCompetenceWithType(long id, LearningResourceType type) throws DbConnectionException;
	
	List<TargetCompetence1> getTargetCompetencesForUser(long userId, Session session) 
			throws DbConnectionException;
	
	void restoreArchivedCompetence(long compId, UserContextData context)
			throws DbConnectionException;

	Result<Void> restoreArchivedCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException;
	
	RestrictedAccessResult<CompetenceData1> getCompetenceForEdit(long credId, long compId, long userId, 
			AccessMode accessMode) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	LearningInfo getCompetenceLearningInfo(long compId, long userId) throws DbConnectionException;
	
	EventQueue updateCompetenceProgress(long targetCompId, UserContextData context)
			throws DbConnectionException;
	
	Result<Void> publishCompetenceIfNotPublished(Competence1 comp, UserContextData context)
			throws DbConnectionException, IllegalDataStateException;

	ResourceCreator getCompetenceCreator(long compId) throws DbConnectionException;

	/**
	 * Returns competencies from credential specified by {@code credId} id that user given by {@code userId} id
	 * started learning.
	 *
	 * @param credId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCompetence1> getTargetCompetencesForCredentialAndUser(long credId, long userId)
			throws DbConnectionException;

	Result<Void> updateCompetenceCreator(long newCreatorId, long oldCreatorId,
										 UserContextData context) throws DbConnectionException;

	List<Tag> getTagsForCompetence(long competenceId) throws DbConnectionException;
	
	/**
	 * Method for getting all completed competences (competences that has progress == 100)
	 * and a hiddenFromProfile flag set to a certain value.
	 * 
	 * @param userId
	 * @param onlyForPublicPublicly - whether to load only credentials mark to be visible on public profile
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCompetenceData> getAllCompletedCompetences(long userId, boolean onlyForPublicPublicly) throws DbConnectionException;
	
	/**
	 * Method for getting all unfinished competences (competences that has progress != 100)
	 * and a hiddenFromProfile flag set to a certain value.
	 * 
	 * @param userId
	 * @param onlyForPublicPublicly - whether to load only credentials mark to be visible on public profile
	 * @return
	 * @throws DbConnectionException
	 */
	List<TargetCompetenceData> getAllInProgressCompetences(long userId, boolean onlyForPublicPublicly) throws DbConnectionException;
	
	/**
	 * Update whether a competence should be visible on the profile or not.
	 * 
	 * @param compId
	 * @param hiddenFromProfile
	 * @throws DbConnectionException
	 */
	void updateHiddenTargetCompetenceFromProfile(long compId, boolean hiddenFromProfile) throws DbConnectionException;

	Result<Void> changeOwnerAndGetEvents(long compId, long newOwnerId, UserContextData context) throws DbConnectionException;

	void changeOwner(long compId, long newOwnerId, UserContextData context) throws DbConnectionException, EventException;

	void disableLearningStagesForOrganizationCompetences(long orgId) throws DbConnectionException;

	LearningPathType getCompetenceLearningPathType(long compId) throws DbConnectionException;
}