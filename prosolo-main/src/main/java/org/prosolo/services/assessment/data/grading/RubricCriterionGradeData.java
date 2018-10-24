package org.prosolo.services.assessment.data.grading;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-10-13
 * @since 1.0.0
 */
public class RubricCriterionGradeData<T extends RubricLevelGradeData> extends RubricItemGradeData {

    private static final long serialVersionUID = -4078621694036398105L;

    private List<T> levels;
    //currently selected level for a student
    private long levelId;
    private String comment;

    public RubricCriterionGradeData() {
        levels = new ArrayList<>();
    }

    public T getSelectedLevel() {
        if (levelId <= 0) {
            return null;
        }
        return levels.stream().filter(l -> l.getId() == levelId).findFirst().get();
    }

    public List<T> getLevels() {
        return levels;
    }

    public void setLevels(List<T> levels) {
        this.levels = levels;
    }

    public long getLevelId() {
        return levelId;
    }

    public void setLevelId(long levelId) {
        this.levelId = levelId;
    }

    public void addLevel(T level) {
        levels.add(level);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
