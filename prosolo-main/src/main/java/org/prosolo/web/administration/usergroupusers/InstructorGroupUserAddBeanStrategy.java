package org.prosolo.web.administration.usergroupusers;

import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserData;

/**
 * Strategy for user group instructors.
 *
 * @author stefanvuckovic
 * @date 2019-08-15
 * @since 1.3.3
 */
public class InstructorGroupUserAddBeanStrategy implements GroupUserAddBeanStrategy {

    private UserGroupManager userGroupManager;
    private UserTextSearch userTextSearch;
    private UnitManager unitManager;

    public InstructorGroupUserAddBeanStrategy(UserGroupManager userGroupManager, UserTextSearch userTextSearch, UnitManager unitManager) {
        this.userGroupManager = userGroupManager;
        this.userTextSearch = userTextSearch;
        this.unitManager = unitManager;
    }

    @Override
    public PaginatedResult<UserData> getCandidatesForAddingToTheGroupFromDb(long unitId, long roleId, long groupId, int offset, int limit) {
        return unitManager.getPaginatedCandidatesForAddingToGroupAsInstructors(
                unitId, roleId, groupId, offset, limit);
    }

    @Override
    public PaginatedResult<UserData> searchCandidatesForAddingToTheGroup(long orgId, long unitId, long roleId, long groupId, String searchTerm, int page, int limit) {
        return userTextSearch.searchCandidatesForAddingToTheGroupAsInstructors(
                orgId, unitId, roleId, groupId, searchTerm, page, limit);
    }

    @Override
    public void addUserToTheGroup(long groupId, long userId, UserContextData context) {
        userGroupManager.addInstructorToTheGroup(groupId, userId, context);
    }
}
