package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;

/**
 * @author stefanvuckovic
 * @date 2018-01-12
 * @since 1.2.0
 */
public class RubricLevelData extends RubricItemData {

    private double points;

    public RubricLevelData(ObjectStatus status) {
        super(status);
    }

    public RubricLevelData(long id, String name, int order) {
        super(id, name, order);
    }

    public RubricLevelData(long id, String name, int order, double points) {
        super(id, name, order);
        this.points = points;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        observeAttributeChange("points", this.points, points);
        this.points = points;
        if (listenChanges) {
            if (arePointsChanged()) {
                setStatus(ObjectStatusTransitions.changeTransition(getStatus()));
            } else if (!hasObjectChanged()) {
                setStatus(ObjectStatusTransitions.upToDateTransition(getStatus()));
            }
        }
    }
}
