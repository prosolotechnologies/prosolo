package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.user.User;

import java.io.Serializable;

/**
 * @author Stefan Vuckovic
 * @date 2017-07-13
 * @since 1.0.0
 */
public class UnitRoleMembershipData implements Serializable {

    private static final long serialVersionUID = -3615538458192525471L;

    private long id;
    private long unitId;
    private long roleId;
    private UserData user;

    public UnitRoleMembershipData(long id, long unitId, long roleId, User user) {
        this.id = id;
        this.unitId = unitId;
        this.roleId = roleId;
        this.user = new UserData(user);
    }

    public long getId() {
        return id;
    }

    public UserData getUser() {
        return user;
    }

    public long getUnitId() {
        return unitId;
    }

    public long getRoleId() {
        return roleId;
    }

}
