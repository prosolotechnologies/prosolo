package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.rubric.visitor.CriterionVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author stefanvuckovic
 * @date 2018-01-10
 * @since 1.2.0
 */
@Entity
public class PointCriterion extends Criterion {

    private double points;

    @Override
    public <T> T accept(CriterionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Column(name = "points", nullable = false, columnDefinition = "double default 0")
    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }
}
