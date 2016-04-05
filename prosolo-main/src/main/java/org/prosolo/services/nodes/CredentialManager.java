package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CredentialData;

public interface CredentialManager {

	Credential1 saveNewCredential(CredentialData data, User createdBy) throws DbConnectionException;
	
	Credential1 deleteCredential(long credId) throws DbConnectionException;
	
	/**
	 * Returns credential data for user -
	 * if user is enrolled it returns his data
	 * with progress
	 * @param credentialId
	 * @param userId
	 * @throws DbConnectionException
	 */
	CredentialData getAllCredentialDataForUser(long credentialId, long userId)
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
	
	Credential1 updateCredential(CredentialData data, User user) throws DbConnectionException;
	
	CredentialData enrollInCredential(long credentialId, User user, LearningContextData context) 
			throws DbConnectionException;
}
