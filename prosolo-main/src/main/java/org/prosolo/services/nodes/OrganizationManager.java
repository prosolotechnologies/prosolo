package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import twitter4j.User;

import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public interface OrganizationManager extends AbstractManager {

    Organization createNewOrganization(String title,List<UserData> adminsChoosen);

    void setUserOrganization(List<UserData>adminsChoosen,Long organizationId);

    Organization getOrganizationByName(String title) throws DbConnectionException;

    Organization getOrganizationById(long id);

    TextSearchResponse1<OrganizationData> getAllOrganizations(int page,int limit);

    List<UserData> getOrganizationAdmins(long organizationId);

    List<UserData> getChoosenAdminsForOrganization(long organizationId);
}

