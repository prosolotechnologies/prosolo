package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

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
}
