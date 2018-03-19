package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.common.observable.StandardObservable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

public class RubricData extends StandardObservable implements Serializable {

    private static final long serialVersionUID = -2193264157221724975L;

    private long id;
    private String name;
    private long organizationId;
    private String creatorFullName;
    private long creatorId;

    private List<RubricCriterionData> criteria;
    private List<RubricItemData> levels;

    private boolean readyToUse;

    private boolean rubricUsed;

    public RubricData() {
        criteria = new ArrayList<>();
        levels = new ArrayList<>();
    }

    public RubricData(Rubric rubric, User creator, boolean isRubricUsed) {
        this.id = rubric.getId();
        this.name = rubric.getTitle();
        this.organizationId = rubric.getOrganization().getId();
        if (creator != null) {
            this.creatorFullName = creator.getFullName();
        }
        this.creatorId = rubric.getCreator().getId();
        this.rubricUsed = isRubricUsed;
    }

    private void syncLevel(RubricItemData level) {
        for (RubricCriterionData crit : criteria) {
            crit.addLevel(level,null);
        }
    }

    private void syncCriterion(RubricCriterionData criterion) {
        for (RubricItemData level : levels) {
            criterion.addLevel(level, null);
        }
    }

    public void syncCriterionWithExistingDescriptions(RubricCriterionData criterion, Map<RubricItemData, String> descriptions) {
        descriptions.forEach((lvl, desc) -> criterion.addLevel(lvl, desc));
    }

    /**
     * Adds level to collection of levels and adds it to all rubric criteria.
     *
     * Should be called when new level (not persistent) is created and should be added to levels collection.
     *
     * @param lvl
     */
    public void addNewLevel(RubricItemData lvl) {
        levels.add(lvl);
        syncLevel(lvl);
    }

    /**
     * Adds criterion to criteria collection and adds all rubric levels to this criterion's levels collection.
     *
     * Should be called when new criterion (not persistent) is created and should be added to criteria collection.
     *
     * @param criterion
     */
    public void addNewCriterion(RubricCriterionData criterion) {
        criteria.add(criterion);
        syncCriterion(criterion);
    }

    public void sortCriteria() {
        sortItems(criteria);
    }

    public void sortLevels() {
        sortItems(levels);
    }

    private <T extends RubricItemData> void sortItems(List<T> items) {
        items.sort(Comparator.comparing(RubricItemData::getOrder));
    }

    public void addCriterion(RubricCriterionData criterion) {
        getCriteria().add(criterion);
    }

    public void addLevel(RubricItemData level) {
        getLevels().add(level);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public String getCreatorFullName() {
        return creatorFullName;
    }

    public void setCreatorFullName(String creatorFullName) {
        this.creatorFullName = creatorFullName;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public List<RubricCriterionData> getCriteria() {
        return criteria;
    }

    public List<RubricItemData> getLevels() {
        return levels;
    }

    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        observeAttributeChange("readyToUse", this.readyToUse, readyToUse);
        this.readyToUse = readyToUse;
    }

    public boolean isReadyToUseChanged() {
        return changedAttributes.containsKey("readyToUse");
    }

    public boolean isRubricUsed() {
        return rubricUsed;
    }

    public void setRubricUsed(boolean rubricUsed) {
        this.rubricUsed = rubricUsed;
    }
}
