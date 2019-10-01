package org.prosolo.web.administration.usergroupusers;

import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.util.roles.SystemRoleNames;

import javax.inject.Inject;

/**
 * Strategy for regular group members.
 *
 * @author stefanvuckovic
 * @date 2019-08-14
 * @since 1.3.3
 */
public class RegularGroupUsersBeanStrategy implements GroupUsersBeanStrategy {

    private UserTextSearch userTextSearch;
    private UserGroupManager userGroupManager;
    private RoleManager roleManager;

    public RegularGroupUsersBeanStrategy(UserTextSearch userTextSearch, UserGroupManager userGroupManager, RoleManager roleManager) {
        this.userTextSearch = userTextSearch;
        this.userGroupManager = userGroupManager;
        this.roleManager = roleManager;
    }

    @Override
    public long getRoleId() {
        return roleManager.getRoleIdByName(SystemRoleNames.USER);
    }

    @Override
    public PaginatedResult<UserData> getUsersFromDb(long groupId, int limit, int offset) {
        return userGroupManager.getPaginatedGroupUsers(groupId, limit, offset);
    }

    @Override
    public PaginatedResult<UserData> searchUsers(long organizationId, String searchTerm, int page, int limit, long groupId) {
        return userTextSearch.searchUsersInGroups(organizationId, searchTerm,page, limit, groupId, false);
    }

    @Override
    public void removeUserFromTheGroup(long groupId, long userId, UserContextData context) {
        userGroupManager.removeUserFromTheGroup(groupId, userId, context);
    }
}
