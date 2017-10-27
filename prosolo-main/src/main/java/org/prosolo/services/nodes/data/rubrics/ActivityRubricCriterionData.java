package org.prosolo.services.nodes.data.rubrics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-10-13
 * @since 1.0.0
 */
public class ActivityRubricCriterionData extends ActivityRubricItemData {

    private static final long serialVersionUID = -4078621694036398105L;

    private List<ActivityRubricLevelData> levels;
    //currently selected level for a student
    private long levelId;
    private String comment;

    public ActivityRubricCriterionData() {
        levels = new ArrayList<>();
    }

    public List<ActivityRubricLevelData> getLevels() {
        return levels;
    }

    public void setLevels(List<ActivityRubricLevelData> levels) {
        this.levels = levels;
    }

    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public void addLevel(ActivityRubricLevelData level) {
        levels.add(level);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
