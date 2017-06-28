package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UserData;

import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public interface OrganizationManager extends AbstractManager {

    Organization createNewOrganization(String title, List<UserData> adminsChosen, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException;

    Result<Organization> createNewOrganizationAndGetEvents(String title, List<UserData> adminsChosen, long creatorId,
                                                           LearningContextData contextData) throws DbConnectionException;

}
