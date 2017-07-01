package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.common.domainmodel.user.User;

import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public interface OrganizationManager extends AbstractManager {

    PaginatedResult<OrganizationData> getAllOrganizations(int page, int limit);

    void deleteOrganization(long organizationId) throws DbConnectionException, EventException;

    List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session, List<Role> roles)
            throws DbConnectionException;

    Organization createNewOrganization(String title, List<UserData> adminsChosen, long creatorId, LearningContextData contextData)
            throws DbConnectionException, EventException;

    Result<Organization> createNewOrganizationAndGetEvents(String title, List<UserData> adminsChosen, long creatorId,
                                                           LearningContextData contextData) throws DbConnectionException;

    Organization getOrganizationById(long organizationId) throws DbConnectionException;

    Organization updateOrganization(long organizationId,String title,List<UserData> chosenUsers,long creatorId,
                                    LearningContextData lcd) throws DbConnectionException,EventException;

    Result<Organization> updateOrganizationAndGetEvents(long organizationId,String title,List<UserData> chosenUsers,long creatorId,
                                                        LearningContextData lcd) throws DbConnectionException,EventException;



}

