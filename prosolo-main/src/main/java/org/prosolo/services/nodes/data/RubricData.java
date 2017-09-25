package org.prosolo.services.nodes.data;

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

    private List<RubricCategoryData> categories;
    private List<RubricItemData> levels;

    public RubricData() {
        categories = new ArrayList<>();
        levels = new ArrayList<>();
    }

    public RubricData(Rubric rubric, User creator) {
        this.id = rubric.getId();
        this.name = rubric.getTitle();
        this.organizationId = rubric.getOrganization().getId();
        if (creator != null) {
            this.creatorFullName = creator.getFullName();
        }
        this.creatorId = rubric.getCreator().getId();
    }

    public void syncLevel(RubricItemData level) {
        if (!level.isItemSynced()) {
            for (RubricCategoryData category : categories) {
                category.addLevel(level,null);
            }
            level.setItemSynced(true);
        }
    }

    public void syncCategory(RubricCategoryData category) {
        if (!category.isItemSynced()) {
            for (RubricItemData level : levels) {
                category.addLevel(level, null);
            }
            category.setItemSynced(true);
        }
    }

    public void syncCategoryWithExistingDescriptions(RubricCategoryData category, Map<RubricItemData, String> descriptions) {
        if (!category.isItemSynced()) {
            descriptions.forEach((lvl, desc) -> category.addLevel(lvl, desc));
            category.setItemSynced(true);
        }
    }

    public void sortCategories() {
        sortItems(categories);
    }

    public void sortLevels() {
        sortItems(levels);
    }

    private <T extends RubricItemData> void sortItems(List<T> items) {
        items.sort(Comparator.comparing(RubricItemData::getOrder));
    }

    public void addCategory(RubricCategoryData category) {
        getCategories().add(category);
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

    public List<RubricCategoryData> getCategories() {
        return categories;
    }

    public List<RubricItemData> getLevels() {
        return levels;
    }
}
