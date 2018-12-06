package org.prosolo.services.authentication;

import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-10-30
 * @since 1.2.0
 */
public interface AuthenticatedUserService {

    /**
     * Method should be called from context where user details are guaranteed to be available
     * without HttpSession object passed.
     *
     * @return
     */
    boolean isUserLoggedIn();

    /**
     * Method should be called from context where user details are guaranteed to be available
     * without HttpSession object passed.
     * @return
     */
    Optional<ProsoloUserDetails> getLoggedInUser();

    /**
     * Method should be called from context where user details are guaranteed to be available
     * without HttpSession object passed.
     *
     * @return
     */
    Optional<Authentication> getUserAuthentication();

    /**
     * Method should be called from context where user details are guaranteed to be available
     * without HttpSession object passed.
     *
     * @param capability
     * @return
     */
    boolean doesUserHaveCapability(String capability);

    /**
     * Returns user session details for currently logged in user (user that triggered the http request)
     *
     * This method should not be called for arbitrary HttpSession because it makes assumption that data
     * is requested for user whose request this is.
     *
     * This method should be used when there is no guarantee that user session data can be retrieved
     * from current context without session object passed.
     *
     * @param session
     * @return
     */
    Optional<ProsoloUserDetails> getCurrentlyLoggedInUser(HttpSession session);

    /**
     * Returns user session details from passed HttpSession object.
     *
     * This method can be called for arbitrary HttpSession no matter from which context it is called.
     *
     * @param session
     * @return
     */
    Optional<ProsoloUserDetails> getUserDetailsFromSession(HttpSession session);

    void refreshUserSessionData();

}
