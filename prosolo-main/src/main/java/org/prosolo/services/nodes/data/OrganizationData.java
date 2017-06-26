package org.prosolo.services.nodes.data;

import org.prosolo.common.domainmodel.organization.Organization;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public class OrganizationData implements Serializable {

    private long id;
    private String title;
    private List<UserData> admins;

    public OrganizationData(){}

    public OrganizationData(Organization organization,List<UserData> users){
        this();
        this.id = organization.getId();
        this.title = organization.getTitle();
        if(users != null){
            for(UserData u : users){
                this.admins.add(u);
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<UserData> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserData> admins) {
        this.admins = admins;
    }
}
