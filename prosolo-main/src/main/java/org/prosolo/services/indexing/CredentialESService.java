package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.credential.Credential1;

public interface CredentialESService  extends AbstractBaseEntityESService {

	/**
	 * @param cred
	 * @param session
	 */
	void saveCredentialNode(Credential1 cred, Session session);

	/**
	 *
	 * @param cred
	 * @param session
	 */
	void updateCredentialNode(Credential1 cred, Session session);
	
	void updateCredentialBookmarks(long organizationId, long credId, Session session);
	
	void updateCredentialUsersWithPrivileges(long organizationId, long credId, Session session);

	void updateStudents(long organizationId, long credId);

	void updateInstructors(long organizationId, long credId, Session session);

	void updateVisibleToAll(long organizationId, long credId, boolean value);

	void updateArchived(long organizationId, long credId, boolean archived);

	void updateCredentialOwner(long organizationId, long credId, long newOwnerId);

	void updateUnitsForOriginalCredentialAndItsDeliveries(long organizationId, long credentialId, Session session);

	void updateDeliveryTimes(long organizationId, Credential1 delivery);

	void updateLearningStageInfo(Credential1 cred);

	void updateCredentialCategory(Credential1 cred);

}
