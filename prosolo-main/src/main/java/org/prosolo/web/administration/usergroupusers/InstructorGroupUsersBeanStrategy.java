package org.prosolo.web.administration.usergroupusers;

import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.UserGroupInstructorRemovalMode;
import org.prosolo.services.util.roles.SystemRoleNames;

import javax.inject.Inject;

/**
 * Strategy for user group instructors.
 *
 * @author stefanvuckovic
 * @date 2019-08-14
 * @since 1.3.3
 */
public class InstructorGroupUsersBeanStrategy implements GroupUsersBeanStrategy {

    private UserTextSearch userTextSearch;
    private UserGroupManager userGroupManager;
    private RoleManager roleManager;

    private UserGroupInstructorRemovalMode instructorRemovalMode;

    public InstructorGroupUsersBeanStrategy(UserTextSearch userTextSearch, UserGroupManager userGroupManager, RoleManager roleManager) {
        this.userTextSearch = userTextSearch;
        this.userGroupManager = userGroupManager;
        this.roleManager = roleManager;
    }

    @Override
    public long getRoleId() {
        return roleManager.getRoleIdByName(SystemRoleNames.INSTRUCTOR);
    }

    @Override
    public PaginatedResult<UserData> getUsersFromDb(long groupId, int limit, int offset) {
        return userGroupManager.getPaginatedGroupInstructors(groupId, limit, offset);
    }

    @Override
    public PaginatedResult<UserData> searchUsers(long organizationId, String searchTerm, int page, int limit, long groupId) {
        return userTextSearch.searchInstructorsInGroups(organizationId, searchTerm, page, limit, groupId);
    }

    @Override
    public void removeUserFromTheGroup(long groupId, long userId, UserContextData context) {
        userGroupManager.removeInstructorFromTheGroup(groupId, userId, instructorRemovalMode, context);
        instructorRemovalMode = null;
    }

    public void setInstructorRemovalMode(UserGroupInstructorRemovalMode instructorRemovalMode) {
        this.instructorRemovalMode = instructorRemovalMode;
    }

    public UserGroupInstructorRemovalMode getInstructorRemovalMode() {
        return instructorRemovalMode;
    }
}
