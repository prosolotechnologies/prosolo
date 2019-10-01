package org.prosolo.web.administration.usergroupusers;

import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.user.data.UserData;

/**
 * Strategy for GroupUserAddBean for actions related to users depending on the user type.
 *
 * @author stefanvuckovic
 * @date 2019-08-15
 * @since 1.3.3
 */
public interface GroupUserAddBeanStrategy {

    PaginatedResult<UserData> getCandidatesForAddingToTheGroupFromDb(
            long unitId, long roleId, long groupId, int offset, int limit);

    PaginatedResult<UserData> searchCandidatesForAddingToTheGroup(
            long orgId, long unitId, long roleId, long groupId, String searchTerm, int page, int limit);

    void addUserToTheGroup(long groupId, long userId, UserContextData context);
}
