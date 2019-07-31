package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.settings.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.organization.*;
import org.prosolo.services.nodes.data.organization.factory.OrganizationDataFactory;
import org.prosolo.services.nodes.factory.LearningResourceLearningStageDataFactory;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.administration.data.RoleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bojan on 6/9/2017.
 */

@Service("org.prosolo.services.nodes.OrganizationManager")
public class OrganizationManagerImpl extends AbstractManagerImpl implements OrganizationManager {

    private static Logger logger = Logger.getLogger(OrganizationManager.class);

    @Autowired
    private EventFactory eventFactory;
    @Autowired
    private UserManager userManager;
    @Autowired
    private RoleManager roleManager;
    @Inject
    private OrganizationDataFactory organizationDataFactory;
    @Inject
    private OrganizationManager self;
    @Inject
    private LearningResourceLearningStageDataFactory learningResourceLearningStageDataFactory;
    @Inject private CredentialManager credManager;
    @Inject private Competence1Manager compManager;

    @Override
    //nt
    public Organization createNewOrganization(OrganizationBasicData organizationBasicData, UserContextData context)
            throws DbConnectionException {
        Result<Organization> res = self.createNewOrganizationAndGetEvents(organizationBasicData, context);
        eventFactory.generateAndPublishEvents(res.getEventQueue());
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> createNewOrganizationAndGetEvents(OrganizationBasicData organizationBasicData, UserContextData context)
            throws DbConnectionException {
        try {
            Organization organization = new Organization();
            organization.setTitle(organizationBasicData.getTitle());
            organization.setPlugins(new HashSet<>());
            saveEntity(organization);

            // initialize organization plugins
            for (OrganizationPluginType type : OrganizationPluginType.values()) {
                OrganizationPlugin plugin = OrganizationPluginFactory.getOrganizationPlugin(type);
                plugin.setOrganization(organization);
                saveEntity(plugin);
                organization.getPlugins().add(plugin);
            }

            saveEntity(organization);
            userManager.setOrganizationForUsers(organizationBasicData.getAdmins(), organization.getId());

            Result<Organization> res = new Result<>();

            res.appendEvent(eventFactory.generateEventData(EventType.Create, context, organization, null, null, null));
            res.setResult(organization);
            return res;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error saving organization");
        }
    }

    @Override
    public void updateLearningStagesPlugin(long orgId, LearningStagesPluginData pluginData, UserContextData context) {
        Result<Void> res = self.updateLearningStagesPluginAndGetEvents(orgId, pluginData, context);
        eventFactory.generateAndPublishEvents(res.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> updateLearningStagesPluginAndGetEvents(long orgId, LearningStagesPluginData pluginData, UserContextData context) {
        //if learning stages are not enabled, we don't update learning stages for organization
        EventQueue queue = EventQueue.newEventQueue();

        try {
            LearningStagesPlugin learningStagesPlugin = (LearningStagesPlugin) persistence.currentManager().get(LearningStagesPlugin.class, pluginData.getPluginId());

            /*
            if learning in stages was enabled and should be disabled now we should remove stages
            from all credentials and competences in this organization
             */
            if (learningStagesPlugin.isEnabled() && !pluginData.isEnabled()) {
                queue.appendEvents(credManager.disableLearningStagesForOrganizationCredentials(orgId, context));
                queue.appendEvents(compManager.disableLearningStagesForOrganizationCompetences(orgId, context));
            }

            learningStagesPlugin.setEnabled(pluginData.isEnabled());

            for (LearningStageData ls : pluginData.getLearningStagesForDeletion()) {
                deleteById(LearningStage.class, ls.getId(), persistence.currentManager());
            }

            /*
            trigger learning stages deletion at this point to avoid name conflict
            for new learning stages with deleted
             */
            persistence.currentManager().flush();

            for (LearningStageData ls : pluginData.getLearningStages()) {
                switch (ls.getStatus()) {
                    case CREATED:
                        Organization org = new Organization();
                        org.setId(orgId);

                        LearningStage newLStage = new LearningStage();
                        newLStage.setOrganization(org);
                        newLStage.setLearningStagesPlugin(learningStagesPlugin);
                        newLStage.setTitle(ls.getTitle());
                        newLStage.setOrder(ls.getOrder());
                        saveEntity(newLStage);
                        break;
                    case CHANGED:
                        LearningStage lStageToChange = (LearningStage) persistence.currentManager().load(LearningStage.class, ls.getId());
                        lStageToChange.setTitle(ls.getTitle());
                        lStageToChange.setOrder(ls.getOrder());
                        break;
                    default:
                        break;
                }
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("DB constraint violation when updating organization learning stages", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error updating the learning stages");
        }
        return Result.of(queue);
    }

    @Override
    @Transactional
    public void updateEvidenceRepositoryPlugin(long orgId, EvidenceRepositoryPluginData pluginData) {
        try {
            EvidenceRepositoryPlugin evidenceRepositoryPlugin = (EvidenceRepositoryPlugin) persistence.currentManager().load(EvidenceRepositoryPlugin.class, pluginData.getPluginId());
            evidenceRepositoryPlugin.setEnabled(pluginData.isEnabled());
            evidenceRepositoryPlugin.setKeywordsEnabled(pluginData.isKeywordsEnabled());
            evidenceRepositoryPlugin.setFileEvidenceEnabled(pluginData.isFileEvidenceEnabled());
            evidenceRepositoryPlugin.setUrlEvidenceEnabled(pluginData.isUrlEvidenceEnabled());
            evidenceRepositoryPlugin.setTextEvidenceEnabled(pluginData.isTextEvidenceEnabled());
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error updating evidence repository plugin");
        }
    }

    @Override
    @Transactional
    public void updateCredentialCategoriesPlugin(long orgId, CredentialCategoriesPluginData pluginData) {
        try {
            CredentialCategoriesPlugin credentialCategoriesPlugin = (CredentialCategoriesPlugin) persistence.currentManager().load(CredentialCategoriesPlugin.class, pluginData.getPluginId());

            credentialCategoriesPlugin.setEnabled(pluginData.isEnabled());

            for (CredentialCategoryData cat : pluginData.getCredentialCategoriesForDeletion()) {
                deleteById(CredentialCategory.class, cat.getId(), persistence.currentManager());
            }

            /*
            trigger credential categories deletion at this point to avoid name conflict
            for new categories with deleted
             */
            persistence.currentManager().flush();

            for (CredentialCategoryData cat : pluginData.getCredentialCategories()) {
                switch (cat.getStatus()) {
                    case CREATED:
                        Organization org = new Organization();
                        org.setId(orgId);

                        CredentialCategory newCat = new CredentialCategory();
                        newCat.setOrganization(org);
                        newCat.setCredentialCategoriesPlugin(credentialCategoriesPlugin);
                        newCat.setTitle(cat.getTitle());
                        saveEntity(newCat);
                        break;
                    case CHANGED:
                        CredentialCategory catToEdit = (CredentialCategory) persistence.currentManager().load(CredentialCategory.class, cat.getId());
                        catToEdit.setTitle(cat.getTitle());
                        break;
                    default:
                        break;
                }
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error("DB constraint violation when updating organization credential categories", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error updating the credential categories");
        }
    }

    @Override
    @Transactional (readOnly = true)
    public OrganizationData getOrganizationForEdit(long organizationId) throws DbConnectionException {
        try{
            String query =
                    "SELECT organization " +
                    "FROM Organization organization " +
                    "LEFT JOIN FETCH organization.plugins " +
                    "WHERE organization.id = :organizationId";

            Organization organization = (Organization) persistence.currentManager()
                .createQuery(query)
                .setLong("organizationId",organizationId)
                .uniqueResult();

            if (organization == null) {
                return null;
            }

            List<LearningStageData> learningStages = getOrganizationLearningStagesData(organizationId);
            List<CredentialCategoryData> credentialCategories = getOrganizationCredentialCategoriesData(organizationId, true, true);
            OrganizationData od = organizationDataFactory.getOrganizationData(organization, null, learningStages, credentialCategories);

            return od;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error retriving organization");
        }
    }

    @Override
    @Transactional (readOnly = true)
    public List<LearningStageData> getOrganizationLearningStagesData(long orgId) {
        try {
            List<LearningStageData> learningStagesData = new ArrayList<>();
            List<LearningStage> res = getOrganizationLearningStages(orgId, false);

            for (LearningStage ls : res) {
                learningStagesData.add(new LearningStageData(
                        ls.getId(), ls.getTitle(), ls.getOrder(), isLearningStageBeingUsed(ls.getId()), true));
            }
            return learningStagesData;
        } catch (Exception e) {
            logger.error("error", e);
            throw new DbConnectionException("Error retrieving organization learning stages");
        }
    }

    private List<LearningStage> getOrganizationLearningStages(long orgId, boolean returnOnlyIfEnabled) throws DbConnectionException {
        String query =
                "SELECT ls " +
                "FROM LearningStage ls " +
                "LEFT JOIN ls.learningStagesPlugin learningStagesPlugin " +
                "LEFT JOIN learningStagesPlugin.organization org " +
                "WHERE org.id = :orgId " ;

        if (returnOnlyIfEnabled) {
            query += "AND learningStagesPlugin.enabled IS TRUE ";
        }

        query += "ORDER BY ls.order ASC";

        @SuppressWarnings("unchecked")
        List<LearningStage> res = persistence.currentManager()
                .createQuery(query)
                .setLong("orgId", orgId)
                .list();

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningResourceLearningStage> getOrganizationLearningStagesForLearningResource(long orgId) throws DbConnectionException {
        try {
            List<LearningStage> learningStages = getOrganizationLearningStages(orgId, true);
            return learningResourceLearningStageDataFactory.getLearningResourceLearningStages(
                    learningStages.stream().map(ls -> new Object[]{ls, null}).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the learning stages");
        }
    }

    private boolean isLearningStageBeingUsed(long learningStageId) {
        //check if it is used in a credential
        String q1 =
                "SELECT 1 FROM Credential1 c WHERE c.learningStage.id = :lsId";
        Integer i = (Integer) persistence.currentManager()
                .createQuery(q1)
                .setLong("lsId", learningStageId)
                .setMaxResults(1)
                .uniqueResult();

        if (i != null) {
            return true;
        }

        String q2 =
                "SELECT 1 FROM Competence1 c WHERE c.learningStage.id = :lsId";
        i = (Integer) persistence.currentManager()
                .createQuery(q2)
                .setLong("lsId", learningStageId)
                .setMaxResults(1)
                .uniqueResult();
        return i != null;
    }

    @Override
    @Transactional (readOnly = true)
    public List<CredentialCategoryData> getOrganizationCredentialCategoriesData(long orgId, boolean loadCategoryUsageInfo, boolean listenChanges) {
        try {
            List<CredentialCategory> categories = getOrganizationCredentialCategories(orgId);
            return categories.stream().map(cat -> {
                if (loadCategoryUsageInfo) {
                    return new CredentialCategoryData(cat.getId(), cat.getTitle(), isCredentialCategoryBeingUsed(cat.getId()), listenChanges);
                } else {
                    return new CredentialCategoryData(cat.getId(), cat.getTitle(), listenChanges);
                }}).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the credential categories");
        }
    }

    private List<CredentialCategory> getOrganizationCredentialCategories(long orgId) throws DbConnectionException {
        String query =
                "SELECT cat " +
                "FROM CredentialCategory cat " +
                "WHERE cat.organization.id = :orgId " +
                "ORDER BY cat.title";

        @SuppressWarnings("unchecked")
        List<CredentialCategory> res = persistence.currentManager()
                .createQuery(query)
                .setLong("orgId", orgId)
                .list();

        return res;
    }

    private boolean isCredentialCategoryBeingUsed(long credCategoryId) {
        String q =
                "SELECT 1 FROM Credential1 c WHERE c.category.id = :cId";
        Integer i = (Integer) persistence.currentManager()
                .createQuery(q)
                .setLong("cId", credCategoryId)
                .setMaxResults(1)
                .uniqueResult();

        return i != null;
    }

    @Override
    public Organization updateOrganizationBasicInfo(long organizationId, OrganizationBasicData organization, UserContextData context)
            throws DbConnectionException {
        Result<Organization> res = self.updateOrganizationBasicInfoAndGetEvents(organizationId, organization, context);
        eventFactory.generateAndPublishEvents(res.getEventQueue());
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> updateOrganizationBasicInfoAndGetEvents(long organizationid, OrganizationBasicData org, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Organization> res = new Result<>();

            Organization organization = loadResource(Organization.class, organizationid);
            organization.setTitle(org.getTitle());

            for (UserData ud : org.getAdmins()) {
                User user = new User(ud.getId());
                switch (ud.getObjectStatus()) {
                    case REMOVED:
                        userManager.setUserOrganization(ud.getId(), 0);
                        res.appendEvent(eventFactory.generateEventData(EventType.USER_REMOVED_FROM_ORGANIZATION, context, user, organization, null, null));
                        break;
                    case CREATED:
                        userManager.setUserOrganization(ud.getId(), organizationid);
                        res.appendEvent(eventFactory.generateEventData(EventType.USER_ASSIGNED_TO_ORGANIZATION, context, user, organization, null, null));
                        break;
                    default:
                        break;
                }
            }

            saveEntity(organization);

            return res;
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error updating the organization");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationData getOrganizationDataWithoutAdmins(long organizationId) {
        String query =
                "SELECT organization " +
                "FROM Organization organization " +
                "WHERE organization.id = :organizationId ";

        Organization organization = (Organization) persistence.currentManager().createQuery(query)
                .setParameter("organizationId",organizationId)
                .uniqueResult();

        OrganizationData res = new OrganizationData(organization.getId(),organization.getTitle());

        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<OrganizationData> getAllOrganizations(int page, int limit, boolean loadAdmins)
            throws DbConnectionException {
        try {
            PaginatedResult<OrganizationData> response = new PaginatedResult<>();

            String query =
                    "SELECT organization " +
                    "FROM Organization organization " +
                    "WHERE organization.deleted IS FALSE ";

            Query q = persistence.currentManager().createQuery(query);
            if (page >= 0 && limit > 0) {
                q.setFirstResult(page * limit);
                q.setMaxResults(limit);
            }

            List<Organization> organizations = q.list();

            for (Organization o : organizations) {
                OrganizationData od;
                if (loadAdmins) {
                    String[] rolesArray = new String[]{SystemRoleNames.ADMIN, SystemRoleNames.SUPER_ADMIN};
                    List<RoleData> adminRoles = roleManager.getRolesByNames(rolesArray);

                    List<User> chosenAdmins = getOrganizationUsers(o.getId(), false, persistence.currentManager(), adminRoles.stream().map(RoleData::getId).collect(Collectors.toList()));
                    List<UserData> listToPass = new ArrayList<>();
                    for (User u : chosenAdmins) {
                        listToPass.add(new UserData(u));
                    }
                    od = new OrganizationData(o, listToPass);
                } else {
                    od = new OrganizationData(o);
                }

                response.addFoundNode(od);
            }
            response.setHitsNumber(getOrganizationsCount());
            return response;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving organization data");
        }
    }

    private Long getOrganizationsCount(){
        String countQuery =
                "SELECT COUNT (organization) " +
                "FROM Organization organization " +
                "WHERE organization.deleted IS FALSE ";

        Query result = persistence.currentManager().createQuery(countQuery);

        return (Long)result.uniqueResult();
    }

    @Override
    public void deleteOrganization(long organizationId) throws DbConnectionException {
        Organization organization = null;
        try {
            organization = loadResource(Organization.class, organizationId);
            organization.setDeleted(true);
            saveEntity(organization);
        } catch (ResourceCouldNotBeLoadedException e) {
            throw new DbConnectionException("Error deleting organization");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session, List<Long> roleIds)
            throws DbConnectionException {
        try {
            boolean filterRoles = roleIds != null && !roleIds.isEmpty();

            StringBuilder sb = new StringBuilder("SELECT DISTINCT user FROM User user ");

            if (filterRoles) {
                sb.append("INNER JOIN user.roles role " +
                        "WITH role.id IN (:roleIds) ");
            }

            sb.append("WHERE user.organization.id = :orgId ");

            if (!returnDeleted) {
                sb.append("AND user.deleted = :boolFalse ");
            }

            sb.append("ORDER BY user.lastname, user.name");

            Query q = session
                    .createQuery(sb.toString())
                    .setLong("orgId", organizationId);

            if (filterRoles) {
                q.setParameterList("roleIds", roleIds);
            }

            if (!returnDeleted) {
                q.setBoolean("boolFalse", false);
            }

            return q.list();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving users");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrganizationTitle(long organizationId) throws DbConnectionException {
        try {
            String query = "SELECT org.title FROM Organization org " +
                           "WHERE org.id = :orgId " +
                           "AND org.deleted IS false";

            return (String) persistence.currentManager()
                    .createQuery(query)
                    .setLong("orgId", organizationId)
                    .uniqueResult();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving organization title");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LearningStageData getLearningStageData(long learningStageId) throws DbConnectionException {
        try {
            LearningStage ls = (LearningStage) persistence.currentManager().load(LearningStage.class, learningStageId);
            return new LearningStageData(ls.getId(), ls.getTitle(), ls.getOrder(), false, false);
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error loading the learning stage");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialCategoryData> getOrganizationCredentialCategoriesData(long organizationId) {
        return getOrganizationCredentialCategoriesData(organizationId, false, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialCategoryData> getUsedOrganizationCredentialCategoriesData(long organizationId) {
        List<CredentialCategoryData> allCategories = getOrganizationCredentialCategoriesData(organizationId, true, false);
        //filter categories to return only those that are being used in at least one credential
        return allCategories.stream().filter(category -> category.isUsed()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAssessmentTokensPlugin(AssessmentTokensPluginData pluginData) {
        try {
            AssessmentsPlugin assessmentTokensPlugin = (AssessmentsPlugin) persistence.currentManager().load(AssessmentsPlugin.class, pluginData.getPluginId());

            assessmentTokensPlugin.setAssessmentTokensEnabled(pluginData.isAssessmentTokensEnabled());
            assessmentTokensPlugin.setPrivateDiscussionEnabled(pluginData.isPrivateDiscussionEnabled());
            assessmentTokensPlugin.setInitialNumberOfTokensGiven(pluginData.getInitialNumberOfTokensGiven());
            assessmentTokensPlugin.setNumberOfEarnedTokensPerAssessment(pluginData.getNumberOfEarnedTokensPerAssessment());
            assessmentTokensPlugin.setNumberOfSpentTokensPerRequest(pluginData.getNumberOfSpentTokensPerRequest());
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error updating the organization token info");
        }
    }

    @Override
    public void resetTokensForAllOrganizationUsers(long organizationId, int numberOfTokens, UserContextData context) {
        Result<Void> res = self.resetTokensForAllOrganizationUsersAndGetEvents(organizationId, numberOfTokens, context);
        eventFactory.generateAndPublishEvents(res.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> resetTokensForAllOrganizationUsersAndGetEvents(long organizationId, int numberOfTokens, UserContextData context) {
        try {
            //retrieve all users not having specified number of tokens since only those users will have number of tokens changed and we should notify them
            List<Long> students = userManager.getIdsOfActiveStudentsNotHavingSpecifiedNumberOfTokens(organizationId, numberOfTokens);
            String query =
                    "UPDATE user u " +
                            "INNER JOIN user_user_role uur " +
                            "ON uur.user = u.id " +
                            "INNER JOIN role r " +
                            "ON r.id = uur.roles " +
                            "AND r.title = :studentRoleName " +
                            "SET u.number_of_tokens = :numberOfTokens WHERE u.organization = :orgId";
            persistence.currentManager()
                    .createSQLQuery(query)
                    .setInteger("numberOfTokens", numberOfTokens)
                    .setLong("orgId", organizationId)
                    .setString("studentRoleName", SystemRoleNames.USER)
                    .executeUpdate();
            Result<Void> res = new Result<>();
            for (long student : students) {
                User eventObj = new User();
                eventObj.setId(student);
                res.appendEvent(eventFactory.generateEventData(EventType.ASSESSMENT_TOKENS_NUMBER_UPDATED, context, eventObj, null, null, null));
            }
            return res;
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error resetting tokens for organization (" + organizationId + ") users");
        }
    }

    @Override
    public void addTokensToAllOrganizationUsers(long organizationId, int numberOfTokens, UserContextData context) {
        Result<Void> res = self.addTokensToAllOrganizationUsersAndGetEvents(organizationId, numberOfTokens, context);
        eventFactory.generateAndPublishEvents(res.getEventQueue());
    }

    @Override
    @Transactional
    public Result<Void> addTokensToAllOrganizationUsersAndGetEvents(long organizationId, int numberOfTokens, UserContextData context) {
        try {
            String query =
                    "UPDATE user u " +
                            "INNER JOIN user_user_role uur " +
                            "ON uur.user = u.id " +
                            "INNER JOIN role r " +
                            "ON r.id = uur.roles " +
                            "AND r.title = :studentRoleName " +
                            "SET u.number_of_tokens = u.number_of_tokens + :numberOfTokens WHERE u.organization = :orgId";
            persistence.currentManager()
                    .createSQLQuery(query)
                    .setInteger("numberOfTokens", numberOfTokens)
                    .setLong("orgId", organizationId)
                    .setString("studentRoleName", SystemRoleNames.USER)
                    .executeUpdate();

            List<Long> students = userManager.getIdsOfActiveStudentsFromOrganization(organizationId);
            Result<Void> res = new Result<>();
            for (long student : students) {
                User eventObj = new User();
                eventObj.setId(student);
                res.appendEvent(eventFactory.generateEventData(EventType.ASSESSMENT_TOKENS_NUMBER_UPDATED, context, eventObj, null, null, null));
            }
            return res;
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error adding tokens to organization (" + organizationId + ") users");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends OrganizationPlugin> T getOrganizationPlugin(Class<T> pluginClass, long organizationId) {
        try {
            String query =
                    "SELECT plugin " +
                    "FROM OrganizationPlugin plugin " +
                    "LEFT JOIN plugin.organization org " +
                    "WHERE org.id = :orgId " +
                    "AND type(plugin) = :type";

            return (T) persistence.currentManager()
                    .createQuery(query)
                    .setLong("orgId", organizationId)
                    .setString("type", pluginClass.getSimpleName())
                    .uniqueResult();
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving organization plugin of type " + pluginClass + " for the organization with id " + organizationId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationPlugin> getAllOrganizationPlugins(long organizationId) {
        try {
            String query =
                    "SELECT plugin " +
                    "FROM OrganizationPlugin plugin " +
                    "LEFT JOIN plugin.organization org " +
                    "WHERE org.id = :orgId";

            return persistence.currentManager()
                    .createQuery(query)
                    .setLong("orgId", organizationId)
                    .list();
        } catch (Exception e){
            logger.error("Error", e);
            throw new DbConnectionException("Error retrieving all organization plugins for the organization with id " + organizationId);
        }
    }

}
