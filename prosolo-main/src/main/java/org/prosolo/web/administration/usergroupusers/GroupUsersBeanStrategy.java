package org.prosolo.web.administration.usergroupusers;

import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.user.data.UserData;

/**
 * Strategy for GroupUsersBean for actions related to users depending on the user type.
 *
 * @author stefanvuckovic
 * @date 2019-08-13
 * @since 1.3.3
 */
public interface GroupUsersBeanStrategy {

    long getRoleId();
    PaginatedResult<UserData> getUsersFromDb(long groupId, int limit, int offset);
    PaginatedResult<UserData> searchUsers(long organizationId, String searchTerm, int page,
            int limit, long groupId);
    void removeUserFromTheGroup(long groupId, long userId, UserContextData context);
}
