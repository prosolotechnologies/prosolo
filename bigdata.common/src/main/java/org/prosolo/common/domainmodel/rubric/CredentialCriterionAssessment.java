package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.assessment.CredentialAssessment;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2018-01-30
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"assessment", "criterion"})})
public class CredentialCriterionAssessment extends CriterionAssessment {

    private CredentialAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    public CredentialAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(CredentialAssessment assessment) {
        this.assessment = assessment;
    }

}
