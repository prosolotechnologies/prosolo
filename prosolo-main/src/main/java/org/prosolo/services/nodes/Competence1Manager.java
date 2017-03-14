package org.prosolo.services.nodes;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.event.EventData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
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
	 * @param data
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 deleteCompetence(CompetenceData1 data, long userId) 
			throws DbConnectionException;
	
	Competence1 updateCompetence(CompetenceData1 data, long userId, 
			LearningContextData context) 
			throws DbConnectionException, CompetenceEmptyException;
	
	Competence1 updateCompetenceData(CompetenceData1 data, long userId);
	
	List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags) 
			throws DbConnectionException;

	List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred) 
			throws DbConnectionException;
	
	
	/**
	 * 
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param loadActivities
	 * @param userId
	 * @param privilege
	 * @param shouldTrackChanges
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IllegalArgumentException
	 * @throws DbConnectionException
	 */
	CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, long userId, UserGroupPrivilege privilege,
			boolean shouldTrackChanges) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException;
	
	List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished, boolean includeCanEdit, long userId) 
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
	void updateDurationForCompetenceWithActivity(long actId, long duration, Operation op) throws DbConnectionException;
	
	/**
	 * New duration for target competence is set. Duration of target credential is not updated.
	 * @param id
	 * @param duration
	 * @throws DbConnectionException
	 */
	void updateTargetCompetenceDuration(long id, long duration) throws DbConnectionException;
	
	String getCompetenceTitle(long id) throws DbConnectionException;
	
	String getTargetCompetenceTitle(long targetCompId) throws DbConnectionException;

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
	 * Returns privilege of a user with {@code userId} id for competence with {@code compId} id.
	 * 
	 * If {@code credId} is greater than 0, privilege for credential is returned, otherwise privilege
	 * for competence is returned.
	 * @param credId
	 * @param compId
	 * @param userId
	 * @return {@link UserGroupPrivilege}
	 */
	UserGroupPrivilege getUserPrivilegeForCompetence(long credId, long compId, long userId) 
			throws DbConnectionException;
	
	boolean isVisibleToAll(long compId) throws DbConnectionException;
	
	void updateCompetenceVisibility(long compId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged) 
    				throws DbConnectionException;

	List<Competence1> getCompetencesForOwner(long ownerId) throws DbConnectionException;

	List<TargetCompetence1> getTargetCompetencesForOwner(long ownerId) throws DbConnectionException;
}