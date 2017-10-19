package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-22
 * @since 1.0.0
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title","rubric"})})
public class Category extends BaseEntity {

    private double points;
    private Rubric rubric;
    private int order;
    private Set<CategoryLevel> levels;

    //assessment category
    private Set<CategoryAssessment> assessments;

    @Column(name = "points", nullable = false)
    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Rubric getRubric() {
        return rubric;
    }

    public void setRubric(Rubric rubric) {
        this.rubric = rubric;
    }

    @Column(name = "category_order", nullable = false)
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<CategoryLevel> getLevels() {
        return levels;
    }

    public void setLevels(Set<CategoryLevel> levels) {
        this.levels = levels;
    }

    @OneToMany(mappedBy = "category")
    public Set<CategoryAssessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<CategoryAssessment> assessments) {
        this.assessments = assessments;
    }
}
