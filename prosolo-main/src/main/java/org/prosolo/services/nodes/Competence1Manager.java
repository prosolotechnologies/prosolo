package org.prosolo.services.nodes;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceBookmark;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.LearningInfo;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.nodes.data.resourceAccess.UserAccessSpecification;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;

public interface Competence1Manager {

	/**
	 * Saves new competence to the database. If {@code credentialId} > 0
	 * it will add new competence to credential with that id. If
	 * you don't want to add competence to credential, just pass
	 * 0 for {@code credentialId}
	 * @param data
	 * @param createdBy
	 * @param credentialId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 saveNewCompetence(CompetenceData1 data, long userId, long credentialId, 
			LearningContextData context) throws DbConnectionException,IllegalDataStateException;
	
	/**
	 * @param data
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 deleteCompetence(CompetenceData1 data, long userId) 
			throws DbConnectionException;
	
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
	Competence1 updateCompetence(CompetenceData1 data, long userId, 
			LearningContextData context) 
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
	
	List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags) 
			throws DbConnectionException;

	List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred) 
			throws DbConnectionException;
	
	
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
			boolean loadTags, boolean loadActivities, boolean includeNotPublished, long userId) 
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
	
	void updateTargetCompetencesWithChangedData(long compId, CompetenceChangeTracker changeTracker) 
			throws DbConnectionException;
	
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
	 * @param userId id of a user that created activity
	 * @throws DbConnectionException, IllegalDataStateException
	 */
	EventData addActivityToCompetence(long compId, Activity1 act, long userId) 
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
	
	/**
	 * New duration for target competence is set. Duration of target credential is not updated.
	 * @param id
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateTargetCompetenceDuration(long id, long duration) throws DbConnectionException;
	
	String getCompetenceTitle(long id) throws DbConnectionException;
	
	void updateProgressForTargetCompetenceWithActivity(long targetActId) 
			throws DbConnectionException;
	
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
	
	/**
	 * this is the method that should be called when you want to publish competences
	 * 
	 * Returns List of data for events that should be generated after transaction commits.
	 * 
	 * @param credId
	 * @param compIds
	 * @param creatorId
	 * @param role
	 * @throws DbConnectionException
	 * @throws CompetenceEmptyException
	 */
	List<EventData> publishCompetences(long credId, List<Long> compIds, long creatorId) 
			throws DbConnectionException, CompetenceEmptyException;
	
	/**
	 * Method for getting all completed competences (competences that has progress == 100)
	 * and a hiddenFromProfile flag set to a certain value
	 * @return 
	 * @throws DbConnectionException
	 */
	List<TargetCompetence1> getAllCompletedCompetences(Long userId, boolean hiddenFromProfile) throws DbConnectionException;
	
	/**
	 * Method for getting all unfinished competences (competences that has progress != 100)
	 * and a hiddenFromProfile flag set to a certain value
	 * @return 
	 * @throws DbConnectionException
	 */
	List<TargetCompetence1> getAllUnfinishedCompetences(Long userId, boolean hiddenFromProfile) throws DbConnectionException;
	
	List<Competence1> getAllCompetences(Session session) 
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
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged) 
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
	
	void bookmarkCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	void deleteCompetenceBookmark(long compId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	List<CompetenceBookmark> getBookmarkedByIds(long compId, Session session) 
			throws DbConnectionException;
	
	List<CompetenceBookmark> getBookmarkedByIds(long compId) throws DbConnectionException;
	
	List<TargetCompetence1> getTargetCompetencesForCompetence(long compId, 
			boolean justUncompleted) throws DbConnectionException;
	
	TargetCompetence1 enrollInCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	CompetenceData1 enrollInCompetenceAndGetCompetenceData(long compId, long userId, 
			LearningContextData context) throws DbConnectionException;
	
	long countNumberOfStudentsLearningCompetence(long compId) throws DbConnectionException;
	
	void archiveCompetence(long compId, long userId, LearningContextData context) throws DbConnectionException;
	
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
	
	long duplicateCompetence(long compId, long userId, LearningContextData context) throws DbConnectionException,
			EventException;
	
	String getCompetenceTitleForCompetenceWithType(long id, LearningResourceType type) throws DbConnectionException;
	
	List<TargetCompetence1> getTargetCompetencesForUser(long userId, Session session) 
			throws DbConnectionException;
	
	void restoreArchivedCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	RestrictedAccessResult<CompetenceData1> getCompetenceForEdit(long credId, long compId, long userId, 
			AccessMode accessMode) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	LearningInfo getCompetenceLearningInfo(long compId, long userId) throws DbConnectionException;
	
	List<EventData> updateCompetenceProgress(long targetCompId, long userId, LearningContextData contextData) 
			throws DbConnectionException;
	
	Result<Void> publishCompetenceIfNotPublished(Competence1 comp, long actorId) 
			throws DbConnectionException, CompetenceEmptyException;
	
}