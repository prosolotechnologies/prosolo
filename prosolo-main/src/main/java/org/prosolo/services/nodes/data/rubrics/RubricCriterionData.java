package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-09-22
 * @since 1.0.0
 */
public class RubricCriterionData extends RubricItemData {

    private double points;

    //collection of levels for criterion
    private Map<RubricLevelData, RubricItemDescriptionData> levels = new HashMap<>();

    public RubricCriterionData(long id, String name, int order) {
        super(id, name, order);
    }

    public RubricCriterionData(long id, String name, int order, double points) {
        super(id, name, order);
        this.points = points;
    }

    public RubricCriterionData(ObjectStatus status) {
        super(status);
    }

    public void addLevel(RubricLevelData level, String description) {
        RubricItemDescriptionData itemDesc = new RubricItemDescriptionData(description);
        if (listenChanges) {
            itemDesc.startObservingChanges();
        }
        levels.put(level, itemDesc);
    }

    public Map<RubricLevelData, RubricItemDescriptionData> getLevels() {
        return levels;
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
