package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.factory.OrganizationDataFactory;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Organization createNewOrganization(String title, List<UserData> adminsChosen, UserContextData context)
            throws DbConnectionException,EventException {

        Result<Organization> res = self.createNewOrganizationAndGetEvents(title,adminsChosen, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> createNewOrganizationAndGetEvents(String title, List<UserData> adminsChosen, UserContextData context)
            throws DbConnectionException {
        try {
            Organization organization = new Organization();
            organization.setTitle(title);

            saveEntity(organization);
            userManager.setOrganizationForUsers(adminsChosen, organization.getId());

            Result<Organization> res = new Result<>();

            res.addEvent(eventFactory.generateEventData(EventType.Create, context, organization, null, null, null));

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

    @Override
    @Transactional (readOnly = true)
    public OrganizationData getOrganizationDataById(long organizationId,List<Role> userRoles) throws DbConnectionException {
        try{
            String query = "SELECT organization " +
                "FROM Organization organization " +
                "WHERE organization.id = :organizationId";

            Organization organization = (Organization)persistence.currentManager()
                .createQuery(query)
                .setLong("organizationId",organizationId)
                .uniqueResult();

            List<User> chosenAdmins = getOrganizationUsers(organization.getId(),false,persistence.currentManager(),userRoles);

            OrganizationData od = organizationDataFactory.getOrganizationData(organization,chosenAdmins);

            return od;
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retriving organization");
        }
    }

    @Override
    public Organization updateOrganization(long organizationId, String title, List<UserData> chosenUsers, UserContextData context)
            throws DbConnectionException, EventException {
        Result<Organization> res = self.updateOrganizationAndGetEvents(organizationId, title, chosenUsers, context);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> updateOrganizationAndGetEvents(long organizationId, String title, List<UserData> chosenUsers,
                                                               UserContextData context) throws DbConnectionException {
        try{
            Result<Organization> res = new Result<>();

            Organization organization = loadResource(Organization.class,organizationId);
            organization.setTitle(title);

            for(UserData ud : chosenUsers){
                User user = new User(ud.getId());
                switch (ud.getObjectStatus()){
                    case REMOVED:
                        userManager.setUserOrganization(ud.getId(),0);
                        res.addEvent(eventFactory.generateEventData(EventType.USER_REMOVED_FROM_ORGANIZATION, context, user, organization, null, null));
                        break;
                    case CREATED:
                        userManager.setUserOrganization(ud.getId(),organizationId);
                        res.addEvent(eventFactory.generateEventData(EventType.USER_ASSIGNED_TO_ORGANIZATION, context, user, organization, null, null));
                        break;
                    default:
                        break;
                }
            }

            saveEntity(organization);
            return res;
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while loading organization");
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
