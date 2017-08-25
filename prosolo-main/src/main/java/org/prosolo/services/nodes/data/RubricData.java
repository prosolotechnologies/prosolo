package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.rubric.Rubric;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

public class RubricData {

    private long id;
    private String name;
    private long organizationId;
    private String creatorFullName;
    private long creatorId;

    public RubricData(){}

    public RubricData(Rubric rubric) {
        this.id = rubric.getId();
        this.name = rubric.getTitle();
        this.organizationId = rubric.getOrganization().getId();
        this.creatorFullName = rubric.getCreator().getFullName();
        this.creatorId = rubric.getCreator().getId();
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
}
