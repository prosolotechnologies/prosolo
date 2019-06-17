package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ObjectStatusTransitions;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.organization.factory.OrganizationDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.page.UseCase;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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
    @Inject
    private PageAccessRightsResolver pageAccessRightsResolver;
    @Inject
    private ApplicationBean appBean;

    private OrganizationData organization;
    private List<UserData> admins;
    private String id;
    private long decodedId;
    private String searchTerm;
    private String[] rolesArray;
    private List<RoleData> adminRoles;
    private List<Long> adminRolesIds = new ArrayList<>();

    private LearningStageData selectedLearningStage;
    private UseCase learningStageUseCase = UseCase.ADD;

    private CredentialCategoryData selectedCategory;
    private UseCase credentialCategoryUseCase = UseCase.ADD;

    private int tokensToReset;
    private int tokensToAdd;

    public void init() {
        logger.debug("initializing");
        admins = new ArrayList<>();
        try {
            decodedId = idEncoder.decodeId(id);

            if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedId).isCanAccess()) {
                rolesArray = new String[] {SystemRoleNames.ADMIN, SystemRoleNames.SUPER_ADMIN};
                adminRoles = roleManager.getRolesByNames(rolesArray);

                for(RoleData r : adminRoles){
                    adminRolesIds.add(r.getId());
                }
                if (decodedId > 0) {
                    initOrgData();
                } else {
                    organization = new OrganizationData();
                    this.organization.setAdmins(new ArrayList<>());
                }
            } else {
                PageUtil.accessDenied();
            }
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error loading the page");
        }
    }

    private void initOrgData() {
        this.organization = organizationManager.getOrganizationForEdit(decodedId, adminRoles.stream().map(RoleData::getId).collect(Collectors.toList()));

        if (organization == null) {
            this.organization = new OrganizationData();
            PageUtil.fireErrorMessage("Organization cannot be found");
        }
    }

    public boolean isLearningInStagesEnabled() {
        return appBean.getConfig().application.pluginConfig.learningInStagesPlugin.enabled;
    }

    public boolean canNewLearningStageBeAdded() {
        return appBean.getConfig().application.pluginConfig.learningInStagesPlugin.maxNumberOfLearningStages >
                organization.getLearningStages().size();
    }

    public void prepareLearningStageForEdit(LearningStageData ls) {
        selectedLearningStage = ls;
        learningStageUseCase = UseCase.EDIT;
    }

    public void prepareAddingNewLearningStage() {
        selectedLearningStage = new LearningStageData(false);
        selectedLearningStage.setStatus(ObjectStatus.CREATED);
        learningStageUseCase = UseCase.ADD;
    }

    public void removeLearningStage(int index) {
        LearningStageData ls = organization.getLearningStages().remove(index);
        ls.setStatus(ObjectStatusTransitions.removeTransition(ls.getStatus()));
        if (ls.getStatus() == ObjectStatus.REMOVED) {
            organization.addLearningStageForDeletion(ls);
        }
        shiftOrderOfLearningStagesUp(index);
    }

    private void shiftOrderOfLearningStagesUp(int index) {
        int size = organization.getLearningStages().size();
        for(int i = index; i < size; i++) {
            LearningStageData ls = organization.getLearningStages().get(i);
            ls.setOrder(ls.getOrder() - 1);
        }
    }

    public boolean isCreateLearningStageUseCase() {
        return learningStageUseCase == UseCase.ADD;
    }

    public void saveLearningStage() {
        if (learningStageUseCase == UseCase.ADD) {
            selectedLearningStage.setOrder(organization.getLearningStages().size() + 1);
            organization.addLearningStage(selectedLearningStage);
        }
        this.selectedLearningStage = null;
    }

    //credential categories administration
    public void prepareCredentialCategoryForEdit(CredentialCategoryData category) {
        selectedCategory = category;
        credentialCategoryUseCase = UseCase.EDIT;
    }

    public void prepareAddingNewCredentialCategory() {
        selectedCategory = new CredentialCategoryData(false);
        selectedCategory.setStatus(ObjectStatus.CREATED);
        credentialCategoryUseCase = UseCase.ADD;
    }

    public void removeCredentialCategory(int index) {
        CredentialCategoryData cat = organization.getCredentialCategories().remove(index);
        cat.setStatus(ObjectStatusTransitions.removeTransition(cat.getStatus()));
        if (cat.getStatus() == ObjectStatus.REMOVED) {
            organization.addCredentialCategoryForDeletion(cat);
        }
    }

    public boolean isCreateCredentialCategoryUseCase() {
        return credentialCategoryUseCase == UseCase.ADD;
    }

    public void saveCredentialCategory() {
        if (credentialCategoryUseCase == UseCase.ADD) {
            organization.addCredentialCategory(selectedCategory);
        }
        this.selectedCategory = null;
    }

    //credential categories administration end

    public void saveOrganizationBasicInfo() {
        if (this.organization.getId() == 0) {
            createNewOrganization();
        } else {
            updateOrganizationBasicInfo();
        }
    }

    public void setAdministrator(UserData userData) {

        Optional<UserData> removedUserOpt = getUserIfPreviouslyRemoved(userData.getId());

        if(removedUserOpt.isPresent()){
            removedUserOpt.get().setObjectStatus(ObjectStatus.UP_TO_DATE);
        }else{
            userData.setObjectStatus(ObjectStatus.CREATED);
            this.organization.getAdmins().add(userData);
            sortOrganizationAdmins();
        }
        searchTerm = "";
    }

    private void sortOrganizationAdmins() {
        Comparator<UserData> comparator = Comparator.comparing(admin -> admin.getLastName());
        comparator = comparator.thenComparing(admin -> admin.getName());
        this.organization.getAdmins().sort(comparator);
    }

    public void createNewOrganization(){
        try {
            if(this.organization.getAdmins() != null && !this.organization.getAdmins().isEmpty()) {
                Organization organization = organizationManager.createNewOrganization(this.organization.getBasicData(), loggedUser.getUserContext(decodedId));

                logger.debug("New Organization (" + organization.getTitle() + ")");

                PageUtil.fireSuccessfulInfoMessageAcrossPages("New organization has been created");
                PageUtil.redirect("/admin/organizations/" + idEncoder.encodeId(organization.getId()) + "/settings");
            }else{
                PageUtil.fireErrorMessage("Error creating the organization");
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error("Error", e);
            FacesContext.getCurrentInstance().validationFailed();
            /* TODO exception - pay attention to this case - we can have several constraints violated
               and we don't know which one is actually violated so we can't generate specific, meaningful
               message. Should we maybe have a specific exception for each constraint
             */
            PageUtil.fireErrorMessage("Error creating the organization");
        } catch (Exception e){
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error creating the organization");
        }
    }

    public void updateOrganizationBasicInfo() {
        try {
            organizationManager.updateOrganizationBasicInfo(organization.getId(), organization.getBasicData(), loggedUser.getUserContext(decodedId));

            logger.debug("Organization (" + organization.getTitle() + ") updated by the user " + loggedUser.getUserId());

            PageUtil.fireSuccessfulInfoMessage("The organization has been updated");
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error updating the organization");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error updating the organization");
        }
    }

    public void updateOrganizationLearningStages() {
        try {
            organizationManager.updateOrganizationLearningStages(organization.getId(), organization.getLearningStageData(), loggedUser.getUserContext(decodedId));

            logger.debug("Organization (" + organization.getTitle() + ") learning stages updated by the user " + loggedUser.getUserId());

            PageUtil.fireSuccessfulInfoMessage("The organization learning stage configuration has been updated");
            try {
                List<LearningStageData> learningStages = organizationManager.getOrganizationLearningStagesData(organization.getId());
                organization.getLearningStageData().resetLearningStages(learningStages);
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error refreshing the data");
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error updating the organization learning stage configuration");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error updating the organization learning stage configuration");
        }
    }

    public void updateOrganizationCategories(){
        try {
            organizationManager.updateOrganizationCredentialCategories(organization.getId(), organization.getCategoryData());

            logger.debug("Organization (" + organization.getTitle() + ") credential categories updated by the user " + loggedUser.getUserId());

            PageUtil.fireSuccessfulInfoMessage("The organization " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " category configuration has been updated");
            try {
                List<CredentialCategoryData> categories = organizationManager.getOrganizationCredentialCategoriesData(organization.getId(), true, true);
                organization.getCategoryData().resetCredentialCategories(categories);
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error refreshing the data");
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error updating the organization " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " category configuration");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error updating the organization " + ResourceBundleUtil.getLabel("credential").toLowerCase() + " category configuration");
        }
    }

    public void updateOrganizationTokenData() {
        try {
            organizationManager.updateOrganizationTokenInfo(organization.getId(), organization.getTokenData());
            logger.debug("Organization (" + organization.getTitle() + ") token info updated by the user " + loggedUser.getUserId());
            PageUtil.fireSuccessfulInfoMessage("The organization assessment token configuration has been updated");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error updating the organization assessment token configuration");
        }
    }

    public void resetTokensForOrganizationUsers() {
        try {
            organizationManager.resetTokensForAllOrganizationUsers(organization.getId(), tokensToReset);
            logger.debug("Tokens reset for all users in organization " + organization.getTitle());
            PageUtil.fireSuccessfulInfoMessage("Assessment tokens has been successfully reset");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error resetting assessment tokens");
        }
    }

    public void addTokensToOrganizationUsers() {
        try {
            organizationManager.addTokensToAllOrganizationUsers(organization.getId(), tokensToAdd);
            logger.debug("Tokens added to all users in organization " + organization.getTitle());
            PageUtil.fireSuccessfulInfoMessage("Assessment tokens has been successfully added to users");
        } catch (DbConnectionException e) {
            logger.error("error", e);
            PageUtil.fireErrorMessage("Error adding assessment tokens to users");
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

    //VALIDATORS

    //learning stage validator
    public void validateLearningStage(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String learningStageName = (String) value;
        for (LearningStageData ls : organization.getLearningStages()) {
            if (ls != selectedLearningStage && ls.getTitle().equals(learningStageName)) {
                FacesMessage msg = new FacesMessage("Learning stage with that name already exists within the organization");
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(msg);
            }
        }
    }

    //credential category validator
    public void validateCredentialCategory(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String categoryName = (String) value;
        boolean duplicateName = organization.getCredentialCategories().stream().anyMatch(cat -> cat != selectedCategory && cat.getTitle().equals(categoryName));
        if (duplicateName) {
            FacesMessage msg = new FacesMessage("Category with that name already exists within the organization");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
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

    public LearningStageData getSelectedLearningStage() {
        return selectedLearningStage;
    }

    public CredentialCategoryData getSelectedCategory() {
        return selectedCategory;
    }

    public int getTokensToReset() {
        return tokensToReset;
    }

    public void setTokensToReset(int tokensToReset) {
        this.tokensToReset = tokensToReset;
    }

    public int getTokensToAdd() {
        return tokensToAdd;
    }

    public void setTokensToAdd(int tokensToAdd) {
        this.tokensToAdd = tokensToAdd;
    }
}
