package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

public interface CredentialESService  extends AbstractBaseEntityESService {

	/**
	 * @param cred 
	 * @param originalVersionId when {@code cred} is a draft version
	 * originalVersionId is needed. Otherwise 0 should be passed.
	 */
	void saveCredentialNode(Credential1 cred, long originalVersionId);
	
	/**
	 * @param cred
	 * @param originalVersionId when {@code cred} is a draft version
	 * originalVersionId is needed. Otherwise 0 should be passed.
	 * @param changeTracker
	 */
	void updateCredentialNode(Credential1 cred, long originalVersionId, 
			CredentialChangeTracker changeTracker);
	
	void updateCredentialDraftVersionCreated(String id);
	
	void addBookmarkToCredentialIndex(long credId, long userId);
	
	void removeBookmarkFromCredentialIndex(long credId, long userId);
	
	void updateCredentialBookmarks(long credId);

}
