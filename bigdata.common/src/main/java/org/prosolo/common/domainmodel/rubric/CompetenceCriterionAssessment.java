package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2018-01-30
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"assessment", "criterion"})})
public class CompetenceCriterionAssessment extends CriterionAssessment {

    private CompetenceAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    public CompetenceAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(CompetenceAssessment assessment) {
        this.assessment = assessment;
    }

}
