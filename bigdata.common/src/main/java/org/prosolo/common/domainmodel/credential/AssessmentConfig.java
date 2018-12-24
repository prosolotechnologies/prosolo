package org.prosolo.common.domainmodel.credential;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

/**
 * @author stefanvuckovic
 * @date 2018-12-13
 * @since 1.2.0
 */
@MappedSuperclass
public class AssessmentConfig extends BaseEntity {

    private AssessmentType assessmentType;
    private BlindAssessmentMode blindAssessmentMode = BlindAssessmentMode.OFF;
    private boolean enabled;

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
