package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;

public interface CredentialESService  extends AbstractBaseEntityESService {

	/**
	 * @param cred 
	 * @param session
	 */
	void saveCredentialNode(Credential1 cred, Session session);
	
	/**
	 * @param cred
	 * @param originalVersionId when {@code cred} is a draft version
	 * originalVersionId is needed. Otherwise 0 should be passed.
	 * @param changeTracker
	 */
	void updateCredentialNode(Credential1 cred, CredentialChangeTracker changeTracker, Session session);
	
	void addBookmarkToCredentialIndex(long credId, long userId);
	
	void removeBookmarkFromCredentialIndex(long credId, long userId);
	
	void updateCredentialBookmarks(long credId, Session session);
	
	void addUserToCredentialIndex(long credId, long userId, UserGroupPrivilege privilege);
	
	void removeUserFromCredentialIndex(long credId, long userId, UserGroupPrivilege privilege);
	
	void addStudentToCredentialIndex(long credId, long userId);
	
	void removeStudentFromCredentialIndex(long credId, long userId);
	
	void updateCredentialUsersWithPrivileges(long credId, Session session);
	
	void updateVisibleToAll(long credId, boolean value);
	
	void addInstructorToCredentialIndex(long credId, long userId);
	
	void removeInstructorFromCredentialIndex(long credId, long userId);

}
