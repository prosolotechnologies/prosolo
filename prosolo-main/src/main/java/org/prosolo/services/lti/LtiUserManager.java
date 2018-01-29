package org.prosolo.services.lti;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;

public interface LtiUserManager {

	User getUserForLaunch(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context) throws DbConnectionException;

}