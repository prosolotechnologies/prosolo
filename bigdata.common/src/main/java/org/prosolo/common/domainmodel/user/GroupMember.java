package org.prosolo.common.domainmodel.user;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * @author stefanvuckovic
 * @date 2019-08-13
 * @since 1.3.3
 */
@MappedSuperclass
public class GroupMember extends BaseEntity {

    private static final long serialVersionUID = -4013937482384853911L;

    private User user;
    private UserGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_group")
    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }
}
