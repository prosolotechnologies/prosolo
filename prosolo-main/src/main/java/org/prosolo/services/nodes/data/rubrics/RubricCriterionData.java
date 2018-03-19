package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.services.nodes.data.ObjectStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-09-22
 * @since 1.0.0
 */
public class RubricCriterionData extends RubricItemData {

    //collection of levels for criterion
    private Map<RubricItemData, RubricItemDescriptionData> levels = new HashMap<>();

    public RubricCriterionData() {
        super();
    }

    public RubricCriterionData(ObjectStatus status) {
        super(status);
    }

    public RubricCriterionData(long id, String name, double points, int order) {
        super(id, name, points, order);
    }


    public void addLevel(RubricItemData level, String description) {
        RubricItemDescriptionData itemDesc = new RubricItemDescriptionData(description);
        if (listenChanges) {
            itemDesc.startObservingChanges();
        }
        levels.put(level, itemDesc);
    }

    public Map<RubricItemData, RubricItemDescriptionData> getLevels() {
        return levels;
    }
}
