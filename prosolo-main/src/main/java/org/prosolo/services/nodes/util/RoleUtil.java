package org.prosolo.services.nodes.util;


import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.util.roles.SystemRoleNames;

/**
 * @author Stefan Vuckovic
 * @date 2017-07-11
 * @since 1.0.0
 */
public class RoleUtil {

    public static boolean isAdminRole(Role role) {
        return SystemRoleNames.ADMIN.equalsIgnoreCase(role.getTitle()) ||
               SystemRoleNames.SUPER_ADMIN.equalsIgnoreCase(role.getTitle());
    }

    public static boolean hasAdminRole(User user) {
        return user.getRoles().stream().anyMatch(r -> SystemRoleNames.ADMIN.equalsIgnoreCase(r.getTitle()) || SystemRoleNames.SUPER_ADMIN.equalsIgnoreCase(r.getTitle()));
    }
}
