package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
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
    public Organization createNewOrganization(String title,List<UserData> adminsChoosen) {
        Organization organization = new Organization();
        try{
            organization.setTitle(title);
            saveEntity(organization);
            userManager.setOrganizationForUsers(adminsChoosen,organization.getId());
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


}
