package org.prosolo.search.util.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-05-31
 * @since 1.2.0
 */
public class UserSearchConfig {

    private final UserScope scope;
    private final UserScopeFilter userScopeFilter;
    private final long roleId;
    private final List<Long> unitIds;

    private UserSearchConfig(UserScope scope, UserScopeFilter userScopeFilter, long roleId, List<Long> unitIds) {
        this.scope = scope;
        this.userScopeFilter = userScopeFilter;
        this.roleId = roleId;
        this.unitIds = new ArrayList<>(unitIds);
    }

    public static UserSearchConfig of(UserScope scope, UserScopeFilter userScopeFilter, long roleId, List<Long> unitIds) {
        return new UserSearchConfig(scope, userScopeFilter, roleId, unitIds);
    }

    public UserScopeFilter getUserScopeFilter() {
        return userScopeFilter;
    }

    public long getRoleId() {
        return roleId;
    }

    public List<Long> getUnitIds() {
        return Collections.unmodifiableList(unitIds);
    }

    public UserScope getScope() {
        return scope;
    }

    public enum UserScope {
        ORGANIZATION,
        FOLLOWERS,
        FOLLOWING
    }
}
