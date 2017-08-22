package org.prosolo.services.nodes.util;


import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.util.roles.RoleNames;

/**
 * @author Stefan Vuckovic
 * @date 2017-07-11
 * @since 1.0.0
 */
public class RoleUtil {

    public static boolean isAdminRole(Role role) {
        return RoleNames.ADMIN.equalsIgnoreCase(role.getTitle()) ||
               RoleNames.SUPER_ADMIN.equalsIgnoreCase(role.getTitle());
    }

    public static boolean hasAdminRole(User user) {
        return user.getRoles().stream().anyMatch(r -> RoleNames.ADMIN.equalsIgnoreCase(r.getTitle()) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(r.getTitle()));
    }
}
