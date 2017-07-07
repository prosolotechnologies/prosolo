package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.factory.OrganizationDataFactory;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Inject
    private OrganizationDataFactory organizationDataFactory;

    private OrganizationData organization;
    private List<UserData> admins;
    private String id;
    private long decodedId;
    private String searchTerm;
    private SelectItem[] allRoles;
    private String[] rolesArray;
    private List<Role> adminRoles;
    private List<Long> adminRolesIds = new ArrayList<>();

    public void init() {
        logger.debug("initializing");
        admins = new ArrayList<UserData>();
        try {
            rolesArray = new String[]{"Admin","Super Admin"};
            adminRoles = roleManager.getRolesByNames(rolesArray);
            for(Role r : adminRoles){
                adminRolesIds.add(r.getId());
            }
            decodedId = idEncoder.decodeId(id);
            if (decodedId > 0) {
                this.organization = organizationManager.getOrganizationDataById(decodedId,adminRoles);

                if (organization == null) {
                    this.organization = new OrganizationData();
                    PageUtil.fireErrorMessage("Organization cannot be found");
                }
            }else{
                organization = new OrganizationData();
                this.organization.setAdmins(new ArrayList<>());
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

        Optional<UserData> removedUserOpt = getUserIfPreviouslyRemoved(userData.getId());

        if(removedUserOpt.isPresent()){
            removedUserOpt.get().setObjectStatus(ObjectStatus.UP_TO_DATE);
        }else{
            userData.setObjectStatus(ObjectStatus.CREATED);
            this.organization.getAdmins().add(userData);
        }
        searchTerm = "";
    }

    public void createNewOrganization(){
        try {
            LearningContextData lcd = PageUtil.extractLearningContextData();

            if(this.organization.getAdmins() != null && !this.organization.getAdmins().isEmpty()) {
                Organization organization = organizationManager.createNewOrganization(this.organization.getTitle(),
                        this.organization.getAdmins(),loggedUser.getUserId(),lcd);

                this.organization.setId(organization.getId());

                logger.debug("New Organization (" + organization.getTitle() + ")");

                PageUtil.fireSuccessfulInfoMessageAcrossPages("Organization successfully saved");
                PageUtil.redirect("/admin/organizations/" + idEncoder.encodeId(organization.getId()) + "/edit");
            }else{
                PageUtil.fireErrorMessage("Error while trying to save organization data");
            }
        }catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save organization data");
        }
    }

    public void updateOrganization(){
        try {
            LearningContextData lcd = PageUtil.extractLearningContextData();
            organizationManager.updateOrganization(this.organization.getId(), this.organization.getTitle(),
                    this.organization.getAdmins(), loggedUser.getUserId(),lcd);

            logger.debug("Organization (" + organization.getTitle() + ") updated by the user " + loggedUser.getUserId());

            PageUtil.fireSuccessfulInfoMessage("Organization updated");
        } catch (DbConnectionException e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to update organization data");
        } catch (EventException e) {
            logger.error(e);
        }
    }

    public void loadUsers() {
        this.admins = null;
        if (searchTerm == null || searchTerm.isEmpty()) {
            admins = null;
        } else {
            try {
                List<UserData> usersToExclude = this.organization.getAdmins().stream()
                        .filter(userData -> userData.getObjectStatus() != ObjectStatus.REMOVED)
                        .collect(Collectors.toList());

                PaginatedResult<UserData> result = userTextSearch.searchUsers(searchTerm, 3, usersToExclude, this.adminRolesIds);

                admins = result.getFoundNodes();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    public boolean isAdminChosenListEmpty(){
        return this.organization.getAdmins().stream()
                .anyMatch(userData -> userData.getObjectStatus() != ObjectStatus.REMOVED);
    }

    public void userReset(UserData admin) {
        searchTerm = "";
        removeUser(admin);
    }

    public Optional<UserData> getUserIfPreviouslyRemoved(long userId) {
        return this.organization.getAdmins().stream()
                .filter(user -> user.getObjectStatus() == ObjectStatus.REMOVED && user.getId() == userId)
                .findFirst();
    }

    public void removeUser(UserData userData) {
        userData.setObjectStatus(ObjectStatusTransitions.removeTransition(userData.getObjectStatus()));
        if (userData.getObjectStatus() != ObjectStatus.REMOVED) {
            this.organization.getAdmins().remove(userData);
        }
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

    public String getId() {
        return id;
    }

    public long getDecodedId() {
        return decodedId;
    }

    public void setDecodedId(long decodedId) {
        this.decodedId = decodedId;
    }

    public void setId(String id) {
        this.id = id;
    }
}
