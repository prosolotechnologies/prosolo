package org.prosolo.services.nodes.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-09-22
 * @since 1.0.0
 */
public class RubricCategoryData extends RubricItemData {

    //list of levels for category
    private Map<RubricItemData, RubricItemDescriptionData> levels = new HashMap<>();

    public RubricCategoryData() {
        super();
    }

    public RubricCategoryData(ObjectStatus status) {
        super(status);
    }

    public RubricCategoryData(long id, String name, double points, int order) {
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
