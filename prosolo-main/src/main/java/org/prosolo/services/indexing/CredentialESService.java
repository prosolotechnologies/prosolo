package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;

public interface CredentialESService  extends AbstractBaseEntityESService {

	/**
	 * @param organizationId
	 * @param cred 
	 * @param session
	 */
	void saveCredentialNode(long organizationId, Credential1 cred, Session session);

	/**
	 *
	 * @param organizationId
	 * @param cred
	 * @param session
	 */
	void updateCredentialNode(long organizationId, Credential1 cred, Session session);
	
	void addBookmarkToCredentialIndex(long organizationId, long credId, long userId);
	
	void removeBookmarkFromCredentialIndex(long organizationId, long credId, long userId);
	
	void updateCredentialBookmarks(long organizationId, long credId, Session session);
	
	void addUserToCredentialIndex(long organizationId, long credId, long userId, UserGroupPrivilege privilege);
	
	void removeUserFromCredentialIndex(long organizationId, long credId, long userId, UserGroupPrivilege privilege);
	
	void addStudentToCredentialIndex(long organizationId, long credId, long userId);
	
	void removeStudentFromCredentialIndex(long organizationId, long credId, long userId);
	
	void updateCredentialUsersWithPrivileges(long organizationId, long credId, Session session);
	
	void updateVisibleToAll(long organizationId, long credId, boolean value);
	
	void addInstructorToCredentialIndex(long organizationId, long credId, long userId);
	
	void removeInstructorFromCredentialIndex(long organizationId, long credId, long userId);
	
	void archiveCredential(long organizationId, long credId);
	
	void restoreCredential(long organizationId, long credId);

	void updateCredentialOwner(long organizationId, long credId, long newOwnerId);

	void addUnitToCredentialIndex(long organizationId, long credId, long unitId);

	void removeUnitFromCredentialIndex(long organizationId, long credId, long unitId);

}
