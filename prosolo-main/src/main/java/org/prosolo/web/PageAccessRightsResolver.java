package org.prosolo.web;

import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-23
 * @since 1.1.0
 */

@Service
public class PageAccessRightsResolver {

    @Inject
    private HttpSession session;

    public ResourceAccessData getAccessRightsForOrganizationPage(long organizationId) {
        LoggedUserBean loggedUser = (LoggedUserBean)session.getAttribute("loggeduser");
        if (loggedUser != null && (loggedUser.getOrganizationId() == organizationId ||
                loggedUser.hasCapability("admin.advanced"))) {
            return new ResourceAccessData(false, true, false, false, false);
        }
        return new ResourceAccessData(false, false, false, false, false);
    }
}
