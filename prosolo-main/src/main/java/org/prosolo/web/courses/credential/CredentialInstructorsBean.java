/**
 *
 */
package org.prosolo.web.courses.credential;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.nodes.data.credential.CredentialIdData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "credentialInstructorsBean")
@Component("credentialInstructorsBean")
@Scope("view")
public class CredentialInstructorsBean implements Serializable, Paginable {

    private static final long serialVersionUID = -4892911343069292524L;

    private static Logger logger = Logger.getLogger(CredentialInstructorsBean.class);


    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private UserTextSearch userTextSearch;
    @Inject
    private CredentialManager credManager;
    @Inject
    private CredentialInstructorManager credInstructorManager;
    @Inject
    private LoggedUserBean loggedUserBean;
    @Inject
    private RoleManager roleManager;
    @Inject
    private StudentAssignBean studentAssignBean;
    @Inject
    private UnitManager unitManager;

    // PARAMETERS
    @Getter @Setter
    private String id;
    @Getter
    private long decodedId;

    @Getter
    private long parentCredId;
    @Getter
    private List<InstructorData> instructors;
    @Getter @Setter
    private String searchTerm = "";
    @Getter @Setter
    private InstructorSortOption sortOption = InstructorSortOption.Date;
    @Getter @Setter
    private int page;
    @Getter
    private PaginationData paginationData = new PaginationData(2);

    private InstructorData instructorForRemoval;
    @Getter @Setter
    private boolean reassignAutomatically = true;

    private String context;
    @Getter
    private CredentialIdData credentialIdData;
    @Getter @Setter
    private InstructorSortOption[] sortOptions;

    //for searching unassigned instructors
    @Getter
    @Setter
    private String instructorSearchTerm;
    @Getter
    private List<UserData> unassignedInstructors;
    private long instructorRoleId;
    private List<Long> unitIds;

    private ResourceAccessData access;

    public void init() {
        sortOptions = InstructorSortOption.values();

        decodedId = idEncoder.decodeId(id);

        if (decodedId > 0) {
            context = "name:CREDENTIAL|id:" + decodedId;
            try {
                credentialIdData = credManager.getCredentialIdData(decodedId, CredentialType.Delivery);
                if (credentialIdData != null) {
                    access = credManager.getResourceAccessData(decodedId, loggedUserBean.getUserId(),
                            ResourceAccessRequirements.of(AccessMode.MANAGER)
                                    .addPrivilege(UserGroupPrivilege.Edit));
                    if (!access.isCanAccess()) {
                        PageUtil.accessDenied();
                    } else {
                        // set the pagination page from the UI
                        if (page > 0) {
                            paginationData.setPage(page);
                        }

                        // retrieve parent credential id used for the learning context
                        parentCredId = credManager.getCredentialIdForDelivery(decodedId);

                        // retrieve instructors
                        searchCredentialInstructors();
                        studentAssignBean.init(decodedId, context);
                    }
                } else {
                    PageUtil.notFound();
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error loading instructor data");
            }
        } else {
            PageUtil.notFound();
        }
    }

    public void searchCredentialInstructors() {
        try {
            if (instructors != null) {
                instructors.clear();
            }

            getCredentialInstructors();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public void searchUnassignedInstructors() {
        try {
            unassignedInstructors = new ArrayList<>();
            PaginatedResult<UserData> result = userTextSearch
                    .searchUsersWithInstructorRole(loggedUserBean.getOrganizationId(), instructorSearchTerm, decodedId,
                            instructorRoleId, unitIds);
            unassignedInstructors = result.getFoundNodes();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void prepareAddingInstructor() {
        try {
            if (instructorRoleId == 0) {
                instructorRoleId = roleManager.getRoleIdByName(SystemRoleNames.INSTRUCTOR);

                //retrieve unit ids for original credential, but only if not already initialized (condition instructorRoleId > 0)
                unitIds = unitManager.getAllUnitIdsCredentialIsConnectedTo(credManager.getCredentialIdForDelivery(decodedId));
            }
            instructorSearchTerm = "";
            searchUnassignedInstructors();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public void prepareInstructorForDelete(InstructorData id) {
        instructorForRemoval = id;
    }

    public void addInstructorToCredential(UserData user) {
        try {
            String page = PageUtil.getPostParameter("page");
            String service = PageUtil.getPostParameter("service");
            PageContextData ctx = new PageContextData(page, context, service);
            credInstructorManager.addInstructorToCredential(decodedId, user.getId(), 0, loggedUserBean.getUserContext(ctx));

            // update cache
            unassignedInstructors.remove(user);

            // reset main instructor list
            paginationData.setPage(1);
            searchTerm = "";
            sortOption = InstructorSortOption.Date;
            paginationData.update((int) credInstructorManager.getCredentialInstructorsCount(decodedId));
            instructors = credInstructorManager.getCredentialInstructors(decodedId, true, paginationData.getLimit(), true);

            PageUtil.fireSuccessfulInfoMessage(ResourceBundleUtil.getLabel("instructor") + " is added");
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error adding " + ResourceBundleUtil.getLabel("instructor").toLowerCase());
        }

    }

    public void getCredentialInstructors() {
        PaginatedResult<InstructorData> searchResponse = userTextSearch.searchInstructors(
                loggedUserBean.getOrganizationId(), searchTerm, paginationData.getPage() - 1, paginationData.getLimit(), decodedId, sortOption, null);

        paginationData.update((int) searchResponse.getHitsNumber());
        instructors = searchResponse.getFoundNodes();
    }

    public void resetAndSearch() {
        paginationData.setPage(1);
        searchCredentialInstructors();
    }

    public void applySortOption(InstructorSortOption sortOption) {
        this.sortOption = sortOption;
        paginationData.setPage(1);
        searchCredentialInstructors();
    }

    public void removeInstructorFromCredential() {
        try {
            PageContextData ctx = new PageContextData(PageUtil.getPage(), PageUtil.getPostParameter("learningContext"), null);

            credInstructorManager.removeInstructorFromCredential(
                    instructorForRemoval.getInstructorId(), decodedId, reassignAutomatically,
                    loggedUserBean.getUserContext(ctx));
            instructorForRemoval = null;

            // if the removed tutor was the only one on the current page (pagination), then decrease the page
            if (paginationData.getPage() != 1 && instructors.size() % paginationData.getLimit() == 1) {
                paginationData.setPage(paginationData.getPage() - 1);
            }
            searchTerm = "";

            searchCredentialInstructors();

            // since ES indices are async refreshed, we will set the pagination based on the state in the DB
            paginationData.update((int) credInstructorManager.getCredentialInstructorsCount(decodedId));

            PageUtil.fireSuccessfulInfoMessage("The " + ResourceBundleUtil.getLabel("instructor").toLowerCase() + " has been removed from the " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error removing " + ResourceBundleUtil.getLabel("instructor").toLowerCase() + " from the " + ResourceBundleUtil.getLabel("delivery").toLowerCase());
        }
    }

    @Override
    public void changePage(int page) {
        if (this.paginationData.getPage() != page) {
            this.paginationData.setPage(page);
            searchCredentialInstructors();
        }
    }

    public boolean canEdit() {
        return access != null && access.isCanEdit();
    }

    /*
     * GETTERS / SETTERS
     */

    public String getCredentialTitle() {
        return credentialIdData.getTitle();
    }

    public long getCredentialId() {
        return decodedId;
    }
}
