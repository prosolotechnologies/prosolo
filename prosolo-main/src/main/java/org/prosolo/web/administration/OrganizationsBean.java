package org.prosolo.web.administration;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;


import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.web.util.pagination.PaginationData;
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
    private OrganizationData organizationToDelete;

    @Getter
    @Setter
    private int page;

    public void init(){
        if (page > 0) {
            paginationData.setPage(page);
        }
        loadOrganizations();
    }

    private void loadOrganizations(){
        this.organizations = new ArrayList<>();
        try{
            PaginatedResult<OrganizationData> res = organizationManager.getAllOrganizations(paginationData.getPage() - 1,
                    paginationData.getLimit(), true);
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

    public void setOrganizationToDelete(OrganizationData organization){
        this.organizationToDelete = organization;
    }

    public void delete(){
        if(organizationToDelete != null){
            try {
                organizationManager.deleteOrganization(this.organizationToDelete.getId());

                PageUtil.fireSuccessfulInfoMessageAcrossPages("The organization " + organizationToDelete.getTitle() + " has been deleted");
                PageUtil.redirect("/admin/organizations");
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error deleting the organization");
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
