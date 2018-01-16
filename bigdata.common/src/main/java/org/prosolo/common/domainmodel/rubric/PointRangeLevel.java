package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.rubric.visitor.LevelVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author stefanvuckovic
 * @date 2018-01-10
 * @since 1.2.0
 */
@Entity
public class PointRangeLevel extends Level {

    private double pointsFrom;
    private double pointsTo;

    @Override
    public <T> T accept(LevelVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Column(nullable = false, columnDefinition = "double default 0")
    public double getPointsFrom() {
        return pointsFrom;
    }

    public void setPointsFrom(double pointsFrom) {
        this.pointsFrom = pointsFrom;
    }

    @Column(nullable = false, columnDefinition = "double default 0")
    public double getPointsTo() {
        return pointsTo;
    }

    public void setPointsTo(double pointsTo) {
        this.pointsTo = pointsTo;
    }
}
