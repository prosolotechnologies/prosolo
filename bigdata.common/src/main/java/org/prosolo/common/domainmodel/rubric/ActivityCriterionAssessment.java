package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-10-11
 * @since 1.0.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"assessment", "criterion"})})
public class ActivityCriterionAssessment extends CriterionAssessment {

    private ActivityAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    public ActivityAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(ActivityAssessment assessment) {
        this.assessment = assessment;
    }

}
