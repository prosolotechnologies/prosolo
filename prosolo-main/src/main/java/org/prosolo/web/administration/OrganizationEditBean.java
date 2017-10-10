package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

            if (loggedUser.getOrganizationId() == decodedId || loggedUser.hasCapability("admin.advanced")) {
                if (decodedId > 0) {
                    this.organization = organizationManager.getOrganizationDataById(decodedId, adminRoles);

                    if (organization == null) {
                        this.organization = new OrganizationData();
                        PageUtil.fireErrorMessage("Organization cannot be found");
                    }
                } else {
                    organization = new OrganizationData();
                    this.organization.setAdmins(new ArrayList<>());
                }
            } else {
                PageUtil.accessDenied();
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
            if(this.organization.getAdmins() != null && !this.organization.getAdmins().isEmpty()) {
                Organization organization = organizationManager.createNewOrganization(this.organization.getTitle(),
                        this.organization.getAdmins(),loggedUser.getUserContext(decodedId));

                this.organization.setId(organization.getId());

                logger.debug("New Organization (" + organization.getTitle() + ")");

                PageUtil.fireSuccessfulInfoMessageAcrossPages("New organization has been created");
                PageUtil.redirect("/admin/organizations/" + idEncoder.encodeId(organization.getId()) + "/settings");
            }else{
                PageUtil.fireErrorMessage("Error creating the organization");
            }
        }catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error(e);
            e.printStackTrace();
            FacesContext.getCurrentInstance().validationFailed();
            PageUtil.fireErrorMessage("Organization with this name already exists");
        } catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error creating the organization");
        }
    }

    public void updateOrganization(){
        try {
            organizationManager.updateOrganization(this.organization.getId(), this.organization.getTitle(),
                    this.organization.getAdmins(), loggedUser.getUserContext(decodedId));

            logger.debug("Organization (" + organization.getTitle() + ") updated by the user " + loggedUser.getUserId());

            PageUtil.fireSuccessfulInfoMessage("The organization has been updated");
        } catch (DbConnectionException e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error updating the organization");
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

                PaginatedResult<UserData> result = userTextSearch.searchUsers(0, searchTerm, 3, usersToExclude, this.adminRolesIds);

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
