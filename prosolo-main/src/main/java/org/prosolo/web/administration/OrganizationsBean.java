package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
    private PaginationData paginationData = new PaginationData();
    private Organization organizationToDelete;

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
            this.paginationData.update((int) res.getHitsNumber());
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
        return this.paginationData;
    }

    public void setOrganizationToDelete(long organizationId){
        this.organizationToDelete = organizationManager.getOrganizationById(organizationId);
    }

    public void delete(){
        if(organizationToDelete != null){
            try {
                organizationManager.deleteOrganization(this.organizationToDelete.getId());
                PageUtil.fireSuccessfulInfoMessage("Organization " + organizationToDelete.getTitle() + " is deleted.");
                organizationToDelete = null;
                ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
                extContext.redirect("/admin/organizations");
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error while trying to delete user");
            }
        }
    }

    public List<OrganizationData> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationData> organizations) {
        this.organizations = organizations;
    }

}
