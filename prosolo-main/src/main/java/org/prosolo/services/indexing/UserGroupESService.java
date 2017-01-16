package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.user.UserGroup;

public interface UserGroupESService extends AbstractBaseEntityESService {

	void saveUserGroup(UserGroup group);

	void addCredential(long groupId, long credId);
	
	void removeCredential(long groupId, long credId);
	
	void addCompetence(long groupId, long compId);
	
	void removeCompetence(long groupId, long compId);
}
