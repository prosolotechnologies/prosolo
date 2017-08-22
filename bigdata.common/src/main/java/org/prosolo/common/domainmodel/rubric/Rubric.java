package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-22
 * @since 1.0.0
 */

@Entity
@Table
public class Rubric extends BaseEntity {

    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

}
