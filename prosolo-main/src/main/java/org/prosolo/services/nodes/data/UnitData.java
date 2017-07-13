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
    private long parentUnitId;
    private List<UnitData> childrenUnits;

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

    @Override
    public int compareTo(UnitData o) {
        return this.getTitle().compareTo(o.getTitle());
    }
}
