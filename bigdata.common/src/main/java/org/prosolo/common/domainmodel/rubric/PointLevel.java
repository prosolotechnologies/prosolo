package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.rubric.visitor.CriterionVisitor;
import org.prosolo.common.domainmodel.rubric.visitor.LevelVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author stefanvuckovic
 * @date 2018-01-10
 * @since 1.2.0
 */
@Entity
public class PointLevel extends Level {

    private double points;

    @Override
    public <T> T accept(LevelVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Column(name = "points", nullable = false, columnDefinition = "double default 0")
    public double getPoints() {
        return points;
    }

    public void setPoints(double points){
        this.points = points;
    }
}
