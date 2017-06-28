package org.prosolo.services.nodes.impl;


import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
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
    private ResourceFactory resourceFactory;
    @Autowired
    private UserManager userManager;
    @Inject
    private OrganizationManager self;

    @Override
    public Organization createNewOrganization(String title, List<UserData> adminsChosen, long creatorId, LearningContextData contextData)
            throws DbConnectionException,EventException {

        Result<Organization> res = self.createNewOrganizationAndGetEvents(title,adminsChosen, creatorId, contextData);
        for (EventData ev : res.getEvents()) {
            eventFactory.generateEvent(ev);
        }
        return res.getResult();
    }

    @Override
    @Transactional
    public Result<Organization> createNewOrganizationAndGetEvents(String title, List<UserData> adminsChosen, long creatorId,
                                                                  LearningContextData contextData) throws DbConnectionException {
        try {
            Organization organization = new Organization();
            organization.setTitle(title);
            saveEntity(organization);
            userManager.setOrganizationForUsers(adminsChosen, organization.getId());

            Result<Organization> res = new Result<>();

            res.addEvent(eventFactory.generateEventData(EventType.Create, creatorId, organization, null, contextData, null));

            res.setResult(organization);

            return res;
        } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
                throw new DbConnectionException("Error while saving organization");
            }
        }
}
