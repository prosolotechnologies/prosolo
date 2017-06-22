package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.common.domainmodel.user.User;
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

    private static Logger logger = Logger.getLogger(OrganizationManager.class)

    @Autowired
    private EventFactory eventFactory;
    @Autowired
    private ResourceFactory resourceFactory;
    @Autowired
    private UserManager userManager;

    @Override
    @Transactional(readOnly = false)
    public Organization createNewOrganization(String title,List<UserData> adminsChosen) {
        try{
            Organization organization = new Organization();
            organization.setTitle(title);

            return saveEntity(organization);
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
            throw  new DbConnectionException("Error while saving organization");
        }
    }

    @Override
    public void setUserOrganization(List<UserData>adminsChosen,Long organizationId) {
        for (UserData user : adminsChosen){
            userManager.setUserOrganization(user.getId(),organizationId);
        }
    }

    @Override
    @Transactional (readOnly = true)
    public Organization getOrganizationByName(String title) throws DbConnectionException {
        try {
            String query =
                    "SELECT organization " +
                    "FROM Organization organization " +
                    "WHERE organization.title = :title";

            Organization organization = (Organization) persistence.currentManager().createQuery(query)
                    .setString("title", title)
                    .uniqueResult();

            return organization;
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
            throw new DbConnectionException("Error while retrieving organization");
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
    public TextSearchResponse1<OrganizationData> getAllOrganizations(int page, int limit) {
        TextSearchResponse1<OrganizationData> response = new TextSearchResponse1<>();

        String query =
                "SELECT DISTINCT organization " +
                "FROM Organization organization " +
                "WHERE organization.deleted IS FALSE ";

        List<Organization> organizations = persistence.currentManager().createQuery(query)
                .setFirstResult(page*limit)
                .setMaxResults(limit)
                .list();

        for(Organization o : organizations){
            List<UserData> chosenAdmins = getOrganizationAdmins(o.getId());
            OrganizationData od = new OrganizationData(o,chosenAdmins);
            response.addFoundNode(od);
        }
        setOrganizationsCount(response);
        return response;
    }


    private void setOrganizationsCount(TextSearchResponse1<OrganizationData> response){
        String countQuery =
                "SELECT COUNT (DISTINCT organization) " +
                        "FROM Organization organization " +
                        "WHERE organization.deleted IS FALSE ";

        Query result = persistence.currentManager().createQuery(countQuery);
        response.setHitsNumber((Long) result.uniqueResult());
    }

    @Override
    public List<UserData> getOrganizationAdmins(long organizationId) {

        List<UserData> result = new ArrayList<>();
        Organization org = getOrganizationById(organizationId);

        for(User u : org.getUsers()){
            UserData ud = new UserData(u);
            result.add(ud);
        }
        return result;
    }

    @Override
    public void deleteOrganization(long organizationId) throws DbConnectionException, EventException {
        Organization organization = getOrganizationById(organizationId);
        organization.setDeleted(true);
        for(User u : organization.getUsers()){
            userManager.setUserOrganization(u.getId(),organizationId);
        }
        saveEntity(organization);
    }

}
