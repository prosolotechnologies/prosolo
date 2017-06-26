package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.aspectj.weaver.ast.Or;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ResourceFactory resourceFactory;
    @Autowired
    private UserManager userManager;

    @Override
    @Transactional(readOnly = false)
    public Organization createNewOrganization(String title,List<UserData> adminsChosen) {
        Organization organization = new Organization();
        try{
            organization.setTitle(title);
            saveEntity(organization);
            organization.setId(organization.getId());
            userManager.setUserOrganization(adminsChosen,organization.getId());
            return organization;

        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
            throw  new DbConnectionException("Error while saving organization");
        }
    }

    @Override
    public Organization getOrganizationById(long id) {
        String query =
                "SELECT organization " +
                        "FROM Organization organization " +
                        "WHERE organization.id = :id";

        Organization organization = (Organization) persistence.currentManager().createQuery(query)
                .setLong("id", id)
                .uniqueResult();

        return organization;
    }

    @Override
    public PaginatedResult<OrganizationData> getAllOrganizations(int page, int limit) {
        PaginatedResult<OrganizationData> response = new PaginatedResult<>();

        String query =
                "SELECT organization " +
                "FROM Organization organization " +
                "WHERE organization.deleted IS FALSE ";

        List<Organization> organizations = persistence.currentManager().createQuery(query)
                .setFirstResult(page*limit)
                .setMaxResults(limit)
                .list();

        for(Organization o : organizations){
            List<UserData> chosenAdmins = getOrganizationUsers(o.getId());
            OrganizationData od = new OrganizationData(o,chosenAdmins);
            response.addFoundNode(od);
        }
        response.setHitsNumber(getOrganizationsCount());
        return response;
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
    public List<UserData> getOrganizationUsers(long organizationId) {

        List<UserData> result = new ArrayList<>();
        Organization organization = null;
        try {
            organization = loadResource(Organization.class, organizationId);
        } catch (ResourceCouldNotBeLoadedException e) {
            e.printStackTrace();
        }

        for(User u : organization.getUsers()){
            UserData ud = new UserData(u);
            result.add(ud);
        }
        return result;
    }

    @Override
    public void deleteOrganization(long organizationId) throws DbConnectionException, EventException {
        Organization organization = null;
        try {
            organization = loadResource(Organization.class, organizationId);
        } catch (ResourceCouldNotBeLoadedException e) {
            e.printStackTrace();
        }
        organization.setDeleted(true);
        saveEntity(organization);
    }

}
