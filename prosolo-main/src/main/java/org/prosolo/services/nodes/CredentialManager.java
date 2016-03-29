package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.data.CredentialData;

public interface CredentialManager {

	Credential1 saveNewCredential(CredentialData data, User createdBy) throws DbConnectionException;
	
	Credential1 deleteCredential(long credId) throws DbConnectionException;
	
	CredentialData getAllCredentialDataForUser(long credentialId, long userId)
			throws DbConnectionException;
	
	CredentialData getCredentialDataForCreator(long credentialId, long creatorId) 
			throws DbConnectionException;
	
	CredentialData getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences) throws DbConnectionException;
	
	Credential1 updateCredential(CredentialData data, User user) throws DbConnectionException;
	
}
