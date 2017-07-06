package org.prosolo.services.nodes.data;

import java.io.Serializable;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
public class UnitData implements Serializable {

    private long id;
    private String title;
    private OrganizationData organization;
    private UnitData parentUnit;

    public UnitData(){}

    public UnitData(long id,String title, OrganizationData organization, UnitData parentUnit) {
        this.id = id;
        this.title = title;
        this.organization = organization;
        this.parentUnit = parentUnit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OrganizationData getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationData organization) {
        this.organization = organization;
    }

    public UnitData getParentUnit() {
        return parentUnit;
    }

    public void setParentUnit(UnitData parentUnit) {
        this.parentUnit = parentUnit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
