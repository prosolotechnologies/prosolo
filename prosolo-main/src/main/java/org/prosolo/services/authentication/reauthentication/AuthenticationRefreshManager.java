package org.prosolo.services.authentication.reauthentication;

import org.springframework.security.core.Authentication;

/**
 * Responsible for refreshing the authentication token by reloading user principal data and recreating token
 * based on the previous token.
 *
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public interface AuthenticationRefreshManager {

    /**
     * Recreates authentication token based on previous token and returns new token. Only principal
     * data is refreshed.
     *
     * @param previousAuthentication
     * @return
     * @throws IllegalArgumentException
     */
    Authentication refreshAuthentication(Authentication previousAuthentication);

}
