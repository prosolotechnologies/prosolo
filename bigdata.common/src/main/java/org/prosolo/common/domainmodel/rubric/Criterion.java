package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.rubric.visitor.CriterionVisitor;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-22
 * @since 1.0.0
 */

@Entity
//unique constraint added from the script
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Criterion extends BaseEntity {

    //private double points;
    private Rubric rubric;
    private int order;
    private Set<CriterionLevel> levels;

    //criterion assessments
    private Set<ActivityCriterionAssessment> assessments;
    private Set<CompetenceCriterionAssessment> compAssessments;
    private Set<CredentialCriterionAssessment> credAssessments;

//    @Column(name = "points", nullable = false)
//    public double getPoints() {
//        return points;
//    }
//
//    public void setPoints(double points) {
//        this.points = points;
//    }

    public <T> T accept(CriterionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Rubric getRubric() {
        return rubric;
    }

    public void setRubric(Rubric rubric) {
        this.rubric = rubric;
    }

    @Column(name = "criterion_order", nullable = false)
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @OneToMany(mappedBy = "criterion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<CriterionLevel> getLevels() {
        return levels;
    }

    public void setLevels(Set<CriterionLevel> levels) {
        this.levels = levels;
    }

    @OneToMany(mappedBy = "criterion")
    public Set<ActivityCriterionAssessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<ActivityCriterionAssessment> assessments) {
        this.assessments = assessments;
    }

    @OneToMany(mappedBy = "criterion")
    public Set<CompetenceCriterionAssessment> getCompAssessments() {
        return compAssessments;
    }

    public void setCompAssessments(Set<CompetenceCriterionAssessment> compAssessments) {
        this.compAssessments = compAssessments;
    }

    @OneToMany(mappedBy = "criterion")
    public Set<CredentialCriterionAssessment> getCredAssessments() {
        return credAssessments;
    }

    public void setCredAssessments(Set<CredentialCriterionAssessment> credAssessments) {
        this.credAssessments = credAssessments;
    }
}
