package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * Created by Bojan on 6/6/2017.
 */

@ManagedBean(name = "adminOrganizations")
@Component("adminOrganizations")
@Scope("view")
public class OrganizationsBean implements Serializable,Paginable {

    protected static Logger logger = Logger.getLogger(OrganizationsBean.class);

    @Inject
    private OrganizationManager organizationManager;

    private List<OrganizationData> organizations;
    private OrganizationData organizationToDelete;
    private PaginationData paginationData = new PaginationData();
    private List<UserData> organizationAdmins;

    public void init(){
        logger.debug("Hello from adminOrganizations bean logger");
        System.out.println("Hello from adminOrganizations bean");
        loadOrganizations();
    }

    private void loadOrganizations(){
        this.organizations = new ArrayList<>();
        try{
            TextSearchResponse1<OrganizationData> res = organizationManager.getAllOrganizations(paginationData.getPage() - 1,
                    paginationData.getLimit());
            organizations = res.getFoundNodes();
        }catch (Exception e){
            logger.error(e);
        }
    }

    @Override
    public void changePage(int page) {
        if(this.paginationData.getPage() != page){
            this.paginationData.setPage(page);
            loadOrganizations();
        }
    }

    @Override
    public PaginationData getPaginationData() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<UserData> choosenAdmins(long organizationId){
        List<UserData> choosenAdmins = new ArrayList<>();
        choosenAdmins = organizationManager.getChoosenAdminsForOrganization(organizationId);
        return choosenAdmins;
    }

    public List<OrganizationData> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationData> organizations) {
        this.organizations = organizations;
    }

    public List<UserData> getOrganizationAdmins() {
        return organizationAdmins;
    }

    public void setOrganizationAdmins(List<UserData> organizationAdmins) {
        this.organizationAdmins = organizationAdmins;
    }
}
