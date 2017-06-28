package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
    private List<UserData> adminsChosen;
    private String id;
    private long decodedId;
    private String searchTerm;
    private UserData admin;
    private SelectItem[] allRoles;
    private String[] rolesArray;
    private List<Role> adminRoles;
    private List<Long> adminRolesIds = new ArrayList<>();

    public void init() {
        logger.debug("initializing");
        try {
            decodedId = idEncoder.decodeId(id);
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
                adminsChosen = new ArrayList<UserData>();
            }
            rolesArray = new String[]{"Admin","Super Admin"};
            adminRoles = roleManager.getRolesByNames(rolesArray);
            for(Role r : adminRoles){
                adminRolesIds.add(r.getId());
            }
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while loading page");
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
        adminsChosen.add(userData);
        searchTerm = "";
    }

    public void createNewOrganization(){
        try {
            LearningContextData lcd = PageUtil.extractLearningContextData();

            if(adminsChosen != null && !adminsChosen.isEmpty()) {
                Organization organization = organizationManager.createNewOrganization(this.organization.getTitle(),
                        adminsChosen,loggedUser.getUserId(),lcd);

                this.organization.setId(organization.getId());

                logger.debug("New Organization (" + organization.getTitle() + ")");

                PageUtil.fireSuccessfulInfoMessageAcrossPages("Organization successfully saved");
                PageUtil.redirect("/admin/organizations");
            }else{
                PageUtil.fireSuccessfulInfoMessage("Organization successfully saved");
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
                PaginatedResult<UserData> result = userTextSearch.searchUsers(searchTerm, 3,adminsChosen,this.adminRolesIds );
                admins = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public void userReset(UserData admin) {
        searchTerm = "";
        adminsChosen.remove(admin);
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

    public List<UserData> getAdminsChosen() {
        return adminsChosen;
    }

    public void setAdminsChosen(List<UserData> adminsChosen) {
        this.adminsChosen = adminsChosen;
    }
}
