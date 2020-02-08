package org.prosolo.services.lti;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;

import java.util.Optional;

public interface LtiUserManager {

    /**
     * Returns user email. If user does not exist, it is created.
     *
     * @param ltiConsumerId
     * @param userId
     * @param name
     * @param lastName
     * @param email
     * @param unitId
     * @param roleName
     * @param userGroupId
     * @param context
     * @return
     * @throws DbConnectionException
     */
    Optional<String> getUserForLaunch(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context) throws DbConnectionException;

    Result<User> getUserForLaunchAndGetEvents(long ltiConsumerId, String userId, String name, String lastName, String email, long unitId, String roleName, long userGroupId, UserContextData context)
            throws DbConnectionException;
}