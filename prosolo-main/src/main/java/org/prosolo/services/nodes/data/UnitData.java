package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
public class UnitData implements Serializable,Comparable<UnitData> {

    private long id;
    private String title;
    private UnitData parentUnit;
    private List<UnitData> childrenUnits;

    public UnitData(){
        childrenUnits = new ArrayList<>();
    }

    public UnitData(long id,String title, UnitData parentUnit) {
        this();
        this.id = id;
        this.title = title;
        this.parentUnit = parentUnit;
    }

    public UnitData(Unit unit){
        this();
        this.id = unit.getId();
        this.title = unit.getTitle();
    }

    public UnitData(Unit unit,UnitData parentUnit){
        this();
        this.id = unit.getId();
        this.title = unit.getTitle();
        this.parentUnit = parentUnit;
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

    @Override
    public int compareTo(UnitData o) {
        return this.getTitle().compareTo(o.getTitle());
    }
}
