package org.prosolo.web;

import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-23
 * @since 1.0.0
 */

@Service
public class PageAccessRightsResolver {

    @Autowired
    private LoggedUserBean loggedUserBean;

    public ResourceAccessData canAccessOrganizationPage(long organizationId) {
        if (loggedUserBean.getOrganizationId() == organizationId || loggedUserBean.hasCapability("admin.advanced")) {
            return new ResourceAccessData(false, true, false, false, false);
        }
        return new ResourceAccessData(false, false, false, false, false);
    }
}
