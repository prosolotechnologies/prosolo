package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.user.UserGroup;

public interface UserGroupESService extends AbstractBaseEntityESService {

	void saveUserGroup(long orgId, UserGroup group);

	void deleteUserGroup(long orgId, long groupId);

}
