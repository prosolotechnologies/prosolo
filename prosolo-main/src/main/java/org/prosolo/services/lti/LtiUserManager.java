package org.prosolo.services.lti;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;

public interface LtiUserManager {

	User getUserForLaunch(long consumerId, String userId, String name, String lastName, String email, long organizationId, long unitId, long userGroupId) throws DbConnectionException;

}