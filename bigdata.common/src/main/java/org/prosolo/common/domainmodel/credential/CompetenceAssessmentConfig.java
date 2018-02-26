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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"competence", "assessment_type"})})
public class CompetenceAssessmentConfig extends BaseEntity {

    private Competence1 competence;
    private AssessmentType assessmentType;
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Competence1 getCompetence() {
        return competence;
    }

    public void setCompetence(Competence1 competence) {
        this.competence = competence;
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
}
