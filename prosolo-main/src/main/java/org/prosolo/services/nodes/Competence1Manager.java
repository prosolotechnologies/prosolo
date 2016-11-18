package org.prosolo.services.nodes;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.common.exception.CompetenceEmptyException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.LearningResourceReturnResultType;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.Role;
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
			LearningContextData context) throws DbConnectionException;
	
	/**
	 * Deletes competence by setting deleted flag to true on original competence and 
	 * deleting draft version of a competence from database if exists.
	 * 
	 * IMPORTANT! Id of original competence should always be passed and not id of a
	 * draft version.
	 * @param originalCompId
	 * @param data
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 deleteCompetence(long originalCompId, CompetenceData1 data, long userId) 
			throws DbConnectionException;
	
	Competence1 updateCompetence(long originalCompId, CompetenceData1 data, long userId, 
			LearningContextData context) 
			throws DbConnectionException, CompetenceEmptyException;
	
	Competence1 updateCompetence(CompetenceData1 data) throws DbConnectionException;
	
	List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags) 
			throws DbConnectionException;

	List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred) 
			throws DbConnectionException;
	
	/**
	 * Returns competence data with specified id. 
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER is passed for {@code returnType}
	 * parameter competence will be returned even if it is first time draft if creator of competence
	 * is user specified by {@code userId}.
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER is passed for {@code returnType}
	 * parameter competence will be returned even if it is first time draft if competence is created by
	 * university.
	 * @param credId when you want to return comeptence that is in a credential id of that credential should
	 * be passed, otherwise pass 0
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param userId
	 * @param returnType
	 * @param shouldTrackChanges
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, long userId, LearningResourceReturnResultType returnType,
			boolean shouldTrackChanges) throws DbConnectionException;
	
	/**
	 * Returns competence with specified id. If competence is first time draft, it is only returned if
	 * creator of competence is user specified by {@code userId}
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param userId
	 * @param shouldTrackChanges
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceDataForUser(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, long userId, boolean shouldTrackChanges) 
					throws DbConnectionException;
	
	/**
	 * Returns competence with specified id. If competence is first time draft, it is only returned if
	 * competence is created by university
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param shouldTrackChanges
	 * @return
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceDataForManager(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean shouldTrackChanges) 
					throws DbConnectionException;
	
	CompetenceData1 getCompetenceDataForEdit(long credId, long competenceId, long creatorId, 
			boolean loadActivities) throws DbConnectionException;
	
	List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished) 
					throws DbConnectionException;
	
	List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean includeNotPublished) 
					throws DbConnectionException;
	
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
	 * Returns data for event that should be generated after transaction is commited.
	 * 
	 * @param compId
	 * @param act
	 * @param userId id of a user that created activity
	 * @throws DbConnectionException
	 */
	EventData addActivityToCompetence(long compId, Activity1 act, long userId) 
			throws DbConnectionException;

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
	void updateDurationForCompetencesWithActivity(long actId, long duration, Operation op) throws DbConnectionException;
	
	/**
	 * New duration for target competence is set. Duration of target credential is not updated.
	 * @param id
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateTargetCompetenceDuration(long id, long duration) throws DbConnectionException;
	
	String getCompetenceTitle(long id) throws DbConnectionException;
	
	String getTargetCompetenceTitle(long targetCompId) throws DbConnectionException;
	
	String getCompetenceDraftOrOriginalTitle(long id) throws DbConnectionException;

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
	CompetenceData1 getFullTargetCompetenceOrCompetenceData(long credId, long compId, 
			long userId) throws DbConnectionException;
	
	/**
	 * Returns draft version of competence if exists, original version otherwise
	 * @param credId
	 * @param competenceId
	 * @param loadCreator
	 * @param loadActivities
	 * @return
	 * @throws DbConnectionException
	 */
	 CompetenceData1 getCurrentVersionOfCompetenceForManager(long credId, long competenceId,
				boolean loadCreator, boolean loadActivities) throws DbConnectionException;
	
	/**
	 * this is the method that should be called when you want to publish competences
	 * 
	 * Returns List of data for events that should be generated after transaction commits.
	 * 
	 * @param compIds
	 * @param creatorId
	 * @param role
	 * @throws DbConnectionException
	 * @throws CompetenceEmptyException
	 */
	List<EventData> publishCompetences(List<Long> compIds, long creatorId, Role role) 
			throws DbConnectionException, CompetenceEmptyException;

	Optional<Long> getCompetenceDraftVersionIdForOriginal(long competenceId) throws DbConnectionException;
	
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
	
	List<Competence1> getAllCompetencesWithTheirDraftVersions(Session session) 
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

}