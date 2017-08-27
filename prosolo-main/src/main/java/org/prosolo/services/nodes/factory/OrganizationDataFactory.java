package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-01
 * @since 1.0.0
 */
@Component
public class OrganizationDataFactory {

    public OrganizationData getOrganizationData(Organization organization,List<User> users) throws NullPointerException{

        List<UserData> userDataList = new ArrayList<>();
        try {
            if (users == null) {
                throw new NullPointerException("Users cannot be null");
            }
            for (User u : users) {
                UserData ud = new UserData(u);
                userDataList.add(ud);
            }
            OrganizationData organizationData = new OrganizationData(organization, userDataList);
            return organizationData;
        }catch (NullPointerException npe){
            throw npe;
        }
    }
}
