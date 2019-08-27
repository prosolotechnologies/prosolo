package org.prosolo.web.courses.userprivilege;

import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.data.LearningResourceType;
import org.prosolo.services.user.data.UserGroupInstructorRemovalMode;

/**
 * Interface which should be implemented by beans dealing with manipulating user privileges for learning resource and
 * defines methods for saving user privileges for a resource, retrieving a flag that determines whether to display
 * option for choosing the user group instructor removal mode when data is saved, methods for getting and setting
 * user group instructor removal mode.
 *
 * @author stefanvuckovic
 * @date 2019-08-23
 * @since 1.3.3
 */
public interface ManageUserPrivilegeAware {

    void saveVisibilityMembersData();
    boolean shouldOptionForChoosingUserGroupInstructorRemovalModeBeDisplayed();
    UserGroupInstructorRemovalMode getUserGroupInstructorRemovalMode();
    void setUserGroupInstructorRemovalMode();
}
