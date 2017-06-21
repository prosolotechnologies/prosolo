package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.search.impl.TextSearchResponse1;
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

    private static Logger logger = Logger.getLogger(OrganizationManager.class);

    @Autowired
    private EventFactory eventFactory;
    @Autowired
    private ResourceFactory resourceFactory;
    @Autowired
    private UserManager userManager;

    @Override
    @Transactional(readOnly = false)
    public Organization createNewOrganization(String title,List<UserData> adminsChoosen) {
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
    public void setUserOrganization(List<UserData>adminsChoosen,Long organizationId) {
        for (UserData user : adminsChoosen){
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
                "FROM Organization organization ";

        List<Organization> organizations = persistence.currentManager().createQuery(query)
                .setFirstResult(page*limit)
                .setMaxResults(limit)
                .list();

        for(Organization o : organizations){
            List<UserData> choosenAdmins = getOrganizationAdmins(o.getId());
            OrganizationData od = new OrganizationData(o,choosenAdmins);
            response.addFoundNode(od);
        }

        return response;
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
    public List<UserData> getChoosenAdminsForOrganization(long organizationId) {
        List<UserData> result = new ArrayList<>();

        Organization organization = getOrganizationById(organizationId);
        for(User u : organization.getUsers()){
            UserData ud = new UserData(u);
            result.add(ud);
        }
        return result;
    }

}
