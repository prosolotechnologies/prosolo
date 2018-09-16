package org.prosolo.common.domainmodel.credential;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2018-01-22
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "assessment_type"})})
public class CredentialAssessmentConfig extends BaseEntity {

    private Credential1 credential;
    private AssessmentType assessmentType;
    private BlindAssessmentMode blindAssessmentMode = BlindAssessmentMode.OFF;
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Credential1 getCredential() {
        return credential;
    }

    public void setCredential(Credential1 credential) {
        this.credential = credential;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "assessment_type")
    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    @Type(type = "true_false")
    @Column(columnDefinition = "char(1) DEFAULT 'F'")
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public BlindAssessmentMode getBlindAssessmentMode() {
        return blindAssessmentMode;
    }

    public void setBlindAssessmentMode(BlindAssessmentMode blindAssessmentMode) {
        this.blindAssessmentMode = blindAssessmentMode;
    }
}
