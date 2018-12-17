package org.prosolo.services.indexing;

import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.elasticsearch.AbstractESIndexer;

public interface UserGroupESService extends AbstractESIndexer {

	void saveUserGroup(long orgId, UserGroup group);

	void deleteUserGroup(long orgId, long groupId);

}
