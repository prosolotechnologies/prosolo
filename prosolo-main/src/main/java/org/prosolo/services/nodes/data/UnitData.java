package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.organization.Unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-07-04
 * @since 0.7
 */
public class UnitData implements Serializable,Comparable<UnitData> {

    private long id;
    private String title;
    private long parentUnitId;
    private List<UnitData> childrenUnits;
    private boolean hasUsers;
    private Unit parentUnit;

    public UnitData(){
        childrenUnits = new ArrayList<>();
    }

    public UnitData(long id,String title, long parentUnitId) {
        this();
        this.id = id;
        this.title = title;
        this.parentUnitId = parentUnitId;
    }

    public UnitData(Unit unit){
        this();
        this.id = unit.getId();
        this.title = unit.getTitle();
    }

    public UnitData(Unit unit,Unit parentUnit){
        this();
        this.id = unit.getId();
        this.title = unit.getTitle();
        this.parentUnit = parentUnit;
    }

    public UnitData(Unit unit,long parentUnitId){
        this();
        this.id = unit.getId();
        this.title = unit.getTitle();
        this.parentUnitId = parentUnitId;
    }

    public void addChildren(List<UnitData> children){
        childrenUnits.addAll(children);
    }

    public List<UnitData> getChildrenUnits() {
        return childrenUnits;
    }

    public void setChildrenUnits(List<UnitData> childrenUnits) {
        this.childrenUnits = childrenUnits;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentUnitId() {
        return parentUnitId;
    }

    public void setParentUnitId(long parentUnitId) {
        this.parentUnitId = parentUnitId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHasUsers() {
        return hasUsers;
    }

    public void setHasUsers(boolean hasUsers) {
        this.hasUsers = hasUsers;
    }

    public Unit getParentUnit() {
        return parentUnit;
    }

    public void setParentUnit(Unit parentUnit) {
        this.parentUnit = parentUnit;
    }

    @Override
    public int compareTo(UnitData o) {
        return this.getTitle().compareTo(o.getTitle());
    }
}
