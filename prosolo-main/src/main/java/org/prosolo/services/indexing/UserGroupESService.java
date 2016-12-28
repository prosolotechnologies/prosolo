package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.user.UserGroup;

public interface UserGroupESService extends AbstractBaseEntityESService {

	void saveUserGroup(UserGroup group);

}
