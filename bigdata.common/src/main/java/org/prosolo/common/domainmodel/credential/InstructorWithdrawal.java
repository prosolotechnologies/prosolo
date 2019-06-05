package org.prosolo.common.domainmodel.credential;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * @author stefanvuckovic
 * @date 2019-01-31
 * @since 1.3
 */
@Entity
public class InstructorWithdrawal extends BaseEntity {

    private TargetCredential1 targetCredential;
    //user withdrawn from being instructor
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public TargetCredential1 getTargetCredential() {
        return targetCredential;
    }

    public void setTargetCredential(TargetCredential1 targetCredential) {
        this.targetCredential = targetCredential;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }
}
