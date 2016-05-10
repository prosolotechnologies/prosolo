package org.prosolo.services.nodes;

import java.util.List;
import java.util.Optional;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

public interface CredentialManager extends AbstractManager {

	Credential1 saveNewCredential(CredentialData data, User createdBy) throws DbConnectionException;
	
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
	Credential1 deleteCredential(long originalCredId, CredentialData data, User user) throws DbConnectionException;
	
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
	 * Returns Credential's draft version data for credential with id: {@code credentialId} 
	 * with user's progress for that credential if user is enrolled. However, id that will be 
	 * set for {@code CredentialData} object is id of original credential version.
	 * @param originalVersionId
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	CredentialData getDraftVersionCredentialDataWithProgressIfExists(long originalVersionId, 
			long userId) throws DbConnectionException;
	
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
	
	Credential1 updateCredential(CredentialData data, User user) throws DbConnectionException;
	
	Credential1 updateCredential(CredentialData data);
	
	CredentialData enrollInCredential(long credentialId, long userId, LearningContextData context) 
			throws DbConnectionException;
	
	/**
	 * Adds competence to credential, updates credential duration. If credential is published, draft version 
	 * for credential is created, competence is added to that draft version and original credential becomes draft. 
	 * If draft version for credential already exists, competence will be attached to existing draft version.
	 * 
	 * @param credentialId
	 * @param comp
	 * @throws DbConnectionException
	 */
	void addCompetenceToCredential(long credentialId, Competence1 comp) 
			throws DbConnectionException;
	
	List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId) 
			throws DbConnectionException;

	void updateTargetCredentialsWithChangedData(long credentialId, CredentialChangeTracker changeTracker) 
			throws DbConnectionException;
	
	List<Tag> getCredentialTags(long credentialId) 
			throws DbConnectionException;
	
	List<Tag> getCredentialHashtags(long credentialId) 
			throws DbConnectionException;

	List<CredentialBookmark> getBookmarkedByIds(long id) throws DbConnectionException;
	
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

	void updateDurationForCredentialsWithCompetence(long compId, long duration, Operation op)
			throws DbConnectionException;

	void updateProgressForTargetCredentialWithCompetence(long targetCompId) throws DbConnectionException;
	
	void updateCredentialAndCompetenceProgressAndNextActivityToLearn(long credId, 
			long targetCompId, long targetActId, long userId) throws DbConnectionException;
	
	String getCredentialDraftOrOriginalTitle(long id) throws DbConnectionException;
	
}
