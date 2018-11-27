package org.prosolo.web;

import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * Should be used only from request thread
 *
 * @author Bojan Trifkovic
 * @date 2017-10-23
 * @since 1.1.0
 */

@Service
public class PageAccessRightsResolver {

    @Inject private AuthenticatedUserService authenticatedUserService;

    public ResourceAccessData getAccessRightsForOrganizationPage(long organizationId) {
        Optional<ProsoloUserDetails> prosoloUserDetails = authenticatedUserService.getLoggedInUser();
        if (prosoloUserDetails.isPresent() && (prosoloUserDetails.get().getOrganizationId() == organizationId ||
                authenticatedUserService.doesUserHaveCapability("admin.advanced"))) {
            return new ResourceAccessData(false, true, false, false, false);
        }
        return new ResourceAccessData(false, false, false, false, false);
    }
}
