package org.prosolo.services.nodes;

import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.Operation;
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
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 saveNewCompetence(CompetenceData1 data, User createdBy, long credentialId) 
			throws DbConnectionException;
	
	/**
	 * Deletes competence by setting deleted flag to true on original competence and 
	 * deleting draft version of a competence from database if exists.
	 * 
	 * IMPORTANT! Id of original competence should always be passed and not id of a
	 * draft version.
	 * @param originalCompId
	 * @param data
	 * @param user
	 * @return
	 * @throws DbConnectionException
	 */
	Competence1 deleteCompetence(long originalCompId, CompetenceData1 data, User user) 
			throws DbConnectionException;
	
	Competence1 updateCompetence(CompetenceData1 data, User user) throws DbConnectionException;
	
	Competence1 updateCompetence(CompetenceData1 data) throws DbConnectionException;
	
	List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags) 
			throws DbConnectionException;

	List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred) 
			throws DbConnectionException;
	
	CompetenceData1 getCompetenceData(long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, boolean shouldTrackChanges) throws DbConnectionException;
	
	CompetenceData1 getCompetenceDataForEdit(long competenceId, long creatorId, 
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

	/**
	 * Sets published to true for all competences from the list that do not have
	 * draft version
	 * @param compIds
	 * @throws DbConnectionException
	 */
	void publishDraftCompetencesWithoutDraftVersion(List<Long> compIds) throws DbConnectionException;
	
	void addActivityToCompetence(long compId, Activity1 act) throws DbConnectionException;

	void updateDuration(long id, long duration, Operation op) throws DbConnectionException;
	
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
	CompetenceData1 getFullTargetCompetenceOrCompetenceData(long credId, long compId, 
			long userId) throws DbConnectionException;

}