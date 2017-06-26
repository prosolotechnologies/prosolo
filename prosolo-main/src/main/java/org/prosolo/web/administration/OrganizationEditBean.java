package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Bojan on 6/6/2017.
 */

@ManagedBean(name = "organizationEditBean")
@Component("organizationEditBean")
@Scope("view")
public class OrganizationEditBean implements Serializable {

    protected static Logger logger = Logger.getLogger(OrganizationEditBean.class);

    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private OrganizationManager organizationManager;
    @Inject
    private UserManager userManager;
    @Inject
    private UserTextSearch userTextSearch;
    @Inject
    private RoleManager roleManager;

    private OrganizationData organization;
    private List<UserData> admins;
    private List<UserData> adminsChoosen;
    private String id;
    private long decodedId;
    private String searchTerm;
    private UserData admin;
    private SelectItem[] allRoles;
    private String[] rolesArray;
    private List<Role> adminRoles;

    public void init() {
        logger.debug("initializing");
        try {
            decodedId = idEncoder.decodeId(id);
            // here check id for edit organization
            if (decodedId > 0) {
                User admin = userManager.getUserWithRoles(decodedId);
                if (admin != null) {
                    this.admin = new UserData(admin);
                    Set<Role> roles = admin.getRoles();
                    if (roles != null) {
                        for (Role r : roles) {
                            this.admin.addRoleId(r.getId());
                        }
                    }
                } else {
                    this.admin = new UserData();
                    PageUtil.fireErrorMessage("Admin cannot be found");
                }
            }else{
                admin = new UserData();
                organization = new OrganizationData();
                admins = new ArrayList<UserData>();
                adminsChoosen = new ArrayList<UserData>();
            }
            rolesArray = new String[]{"Admin","Super Admin"};
            adminRoles = roleManager.getRolesByNames(rolesArray);

            prepareRoles();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while loading page");
        }
    }


    private void prepareRoles() {
        try {
            if (adminRoles != null) {
                allRoles = new SelectItem[adminRoles.size()];

                for (int i = 0; i < adminRoles.size(); i++) {
                    Role r = adminRoles.get(i);
                    SelectItem selectItem = new SelectItem(r.getId(), r.getTitle());
                    allRoles[i] = selectItem;
                }
            }
        } catch (DbConnectionException e) {
            logger.error(e);
        }

    }

    public void saveOrganization(){
        if(this.organization.getId() == 0){
            createNewOrganization();
        }else{
            updateOrganization();
        }
    }

    public void setAdministrator(UserData userData) {
        adminsChoosen.add(userData);
    }

    public void createNewOrganization(){
        try {
            if(adminsChoosen != null && !adminsChoosen.isEmpty()) {
                Organization organization = organizationManager.createNewOrganization(this.organization.getTitle(),
                        this.organization.getId(), adminsChoosen);

                this.organization.setId(organization.getId());

                logger.debug("New Organization (" + organization.getTitle() + ")");

                PageUtil.fireSuccessfulInfoMessage("Organization successfully saved");
            }else{
                PageUtil.fireErrorMessage("At least one admin should be selected");
            }
        }catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save organization data");
        }
    }

    public void updateOrganization(){

    }

    public void loadUsers() {
        this.admins = null;
        if (searchTerm == null || searchTerm.isEmpty()) {
            admins = null;
        } else {
            try {
                PaginatedResult<UserData> result = userTextSearch.searchNewOwner(searchTerm, 3,null, adminsChoosen,this.adminRoles );
                admins = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public void userReset(UserData admin) {
        searchTerm = "";
        adminsChoosen.remove(admin);
    }


    public void resetAndSearch() {
        loadUsers();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public OrganizationData getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationData organization) {
        this.organization = organization;
    }

    public List<UserData> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserData> admins) {
        this.admins = admins;
    }

    public List<UserData> getAdminsChoosen() {
        return adminsChoosen;
    }

    public void setAdminsChoosen(List<UserData> adminsChoosen) {
        this.adminsChoosen = adminsChoosen;
    }
}
