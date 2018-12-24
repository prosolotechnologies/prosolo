package org.prosolo.common.domainmodel.studentprofile;

import org.hibernate.annotations.DiscriminatorOptions;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;

/**
 * Represents any resource to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        discriminatorType = DiscriminatorType.INTEGER,
        columnDefinition = "TINYINT"
)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"credential_profile_config", "credential_assessment"}),
        @UniqueConstraint(columnNames = {"credential_profile_config", "target_competence"}),
        @UniqueConstraint(columnNames = {"competence_profile_config", "competence_assessment"}),
        @UniqueConstraint(columnNames = {"competence_profile_config", "competence_evidence"})
})
//TODO check if this annotation is needed in later hibernate versions and if not remove it since it is hibernate specific
@DiscriminatorOptions(force = true)
public abstract class StudentProfileConfig extends BaseEntity {

    private static final long serialVersionUID = 5483843339820996516L;

    private User student;
    private TargetCredential1 targetCredential;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    public TargetCredential1 getTargetCredential() {
        return targetCredential;
    }

    public void setTargetCredential(TargetCredential1 targetCredential) {
        this.targetCredential = targetCredential;
    }
}
