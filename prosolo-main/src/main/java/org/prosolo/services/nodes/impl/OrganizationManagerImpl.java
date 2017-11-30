package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.organization.factory.OrganizationDataFactory;
import org.prosolo.services.nodes.factory.LearningResourceLearningStageDataFactory;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
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
    public Organization createNewOrganization(OrganizationData org, UserContextData context)
            throws DbConnectionException {
        Result<Organization> res = self.createNewOrganizationAndGetEvents(org, context);
        eventFactory.generateEvents(res.getEventQueue());
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> createNewOrganizationAndGetEvents(OrganizationData org, UserContextData context)
            throws DbConnectionException {
        try {
            Organization organization = new Organization();
            organization.setTitle(org.getTitle());

            saveEntity(organization);
            userManager.setOrganizationForUsers(org.getAdmins(), organization.getId());

            Result<Organization> res = new Result<>();

            res.appendEvent(eventFactory.generateEventData(EventType.Create, context, organization, null, null, null));

            res.appendEvents(updateOrganizationLearningStages(organization.getId(), org, context));

            res.setResult(organization);
            return res;
        }catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            throw e;
        }catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while saving organization");
        }
    }

    /**
     *
     * @param orgId
     * @param organization
     *
     * @throws ConstraintViolationException
     * @throws DataIntegrityViolationException
     * @throws DbConnectionException
     */
    private EventQueue updateOrganizationLearningStages(long orgId, OrganizationData organization, UserContextData context) {
        //if learning stages are not enabled, we don't update learning stages for organization
        EventQueue queue = EventQueue.newEventQueue();
        if (Settings.getInstance().config.application.pluginConfig.learningInStagesPlugin.enabled) {
            try {
                Organization org = (Organization) persistence.currentManager().load(Organization.class, orgId);
                /*
                if learning in stages was enabled and should be disabled now we should remove stages
                from all credentials and competences in this organization
                 */
                if (org.isLearningInStagesEnabled() && !organization.isLearningInStagesEnabled()) {
                    queue.appendEvents(credManager.disableLearningStagesForOrganizationCredentials(orgId, context));
                    queue.appendEvents(compManager.disableLearningStagesForOrganizationCompetences(orgId, context));
                }
                org.setLearningInStagesEnabled(organization.isLearningInStagesEnabled());
                for (LearningStageData ls : organization.getLearningStagesForDeletion()) {
                    deleteById(LearningStage.class, ls.getId(), persistence.currentManager());
                }

                /*
                trigger learning stages deletion at this point to avoid name conflict
                for new learning stages with deleted
                 */
                persistence.currentManager().flush();

                for (LearningStageData ls : organization.getLearningStages()) {
                    switch (ls.getStatus()) {
                        case CREATED:
                            LearningStage newLStage = new LearningStage();
                            newLStage.setOrganization(org);
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
        }
        return queue;
    }

    @Override
    @Transactional (readOnly = true)
    public OrganizationData getOrganizationForEdit(long organizationId, List<Role> userRoles) throws DbConnectionException {

        try{
            String query = "SELECT organization " +
                "FROM Organization organization " +
                "WHERE organization.id = :organizationId";

            Organization organization = (Organization)persistence.currentManager()
                .createQuery(query)
                .setLong("organizationId",organizationId)
                .uniqueResult();

            if (organization == null) {
                return null;
            }

            List<User> chosenAdmins = getOrganizationUsers(organization.getId(),false,persistence.currentManager(),userRoles);
            List<LearningStageData> learningStages = getOrganizationLearningStagesData(organizationId);
            OrganizationData od = organizationDataFactory.getOrganizationData(organization,chosenAdmins, learningStages);

            return od;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retriving organization");
        }
    }

    private List<LearningStageData> getOrganizationLearningStagesData(long orgId) {
        List<LearningStageData> learningStagesData = new ArrayList<>();
        //only if learning in stages is enabled load the stages
        if (Settings.getInstance().config.application.pluginConfig.learningInStagesPlugin.enabled) {
            List<LearningStage> res =  getOrganizationLearningStages(orgId, false);

            for (LearningStage ls : res) {
                learningStagesData.add(new LearningStageData(
                        ls.getId(), ls.getTitle(), ls.getOrder(), isLearningStageBeingUsed(ls.getId()), true));
            }
        }
        return learningStagesData;
    }

    private List<LearningStage> getOrganizationLearningStages(long orgId, boolean returnOnlyIfEnabled) throws DbConnectionException {
        String query =
                "SELECT ls " +
                "FROM LearningStage ls ";
        if (returnOnlyIfEnabled) {
            query +=
                    "INNER JOIN ls.organization org " +
                    "WHERE org.id = :orgId " +
                    "AND org.learningInStagesEnabled IS TRUE ";
        } else {
            query +=
                    "WHERE ls.organization.id = :orgId ";
        }

        query += "order by ls.order ASC";

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
            if (Settings.getInstance().config.application.pluginConfig.learningInStagesPlugin.enabled) {
                List<LearningStage> learningStages = getOrganizationLearningStages(orgId, true);
                return learningResourceLearningStageDataFactory.getLearningResourceLearningStages(
                        learningStages.stream().map(ls -> new Object[]{ls, null}).collect(Collectors.toList()));
            }
            return new ArrayList<>();
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
    public Organization updateOrganization(OrganizationData organization, UserContextData context)
            throws DbConnectionException {
        Result<Organization> res = self.updateOrganizationAndGetEvents(organization, context);
        eventFactory.generateEvents(res.getEventQueue());
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> updateOrganizationAndGetEvents(OrganizationData org, UserContextData context)
            throws DbConnectionException, ConstraintViolationException, DataIntegrityViolationException {
        try {
            Result<Organization> res = new Result<>();

            Organization organization = loadResource(Organization.class, org.getId());
            organization.setTitle(org.getTitle());

            for (UserData ud : org.getAdmins()) {
                User user = new User(ud.getId());
                switch (ud.getObjectStatus()) {
                    case REMOVED:
                        userManager.setUserOrganization(ud.getId(), 0);
                        res.appendEvent(eventFactory.generateEventData(EventType.USER_REMOVED_FROM_ORGANIZATION, context, user, organization, null, null));
                        break;
                    case CREATED:
                        userManager.setUserOrganization(ud.getId(), org.getId());
                        res.appendEvent(eventFactory.generateEventData(EventType.USER_ASSIGNED_TO_ORGANIZATION, context, user, organization, null, null));
                        break;
                    default:
                        break;
                }
            }

            saveEntity(organization);

            res.appendEvents(updateOrganizationLearningStages(org.getId(), org, context));

            return res;
        } catch (ConstraintViolationException|DataIntegrityViolationException e) {
            logger.error("Error", e);
            throw e;
        } catch (Exception e){
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error updating the organization");
        }
    }

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
                    List<Role> adminRoles = roleManager.getRolesByNames(rolesArray);

                    List<User> chosenAdmins = getOrganizationUsers(o.getId(), false, persistence.currentManager(), adminRoles);
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
            throw new DbConnectionException("Error while retrieving organization data");
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
            throw new DbConnectionException("Error while deleting organization");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session, List<Role> roles)
            throws DbConnectionException {
        try {
            boolean filterRoles = roles != null && !roles.isEmpty();

            StringBuilder sb = new StringBuilder("SELECT DISTINCT user FROM User user ");

            if (filterRoles) {
                sb.append("INNER JOIN user.roles role " +
                        "WITH role IN (:roles) ");
            }

            sb.append("WHERE user.organization.id = :orgId ");

            if (!returnDeleted) {
                sb.append("AND user.deleted = :boolFalse");
            }

            Query q = session
                    .createQuery(sb.toString())
                    .setLong("orgId", organizationId);

            if (filterRoles) {
                q.setParameterList("roles", roles);
            }

            if (!returnDeleted) {
                q.setBoolean("boolFalse", false);
            }

            return q.list();
        } catch (Exception e) {
            logger.error("Error", e);
            throw new DbConnectionException("Error while retrieving users");
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
            throw new DbConnectionException("Error while retrieving organization title");
        }
    }

}
