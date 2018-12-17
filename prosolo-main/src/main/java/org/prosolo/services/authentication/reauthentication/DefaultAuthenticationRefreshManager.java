package org.prosolo.services.authentication.reauthentication;

import org.prosolo.services.authentication.reauthentication.providers.AuthenticationRefreshProvider;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Refreshes and returns new authentication token by delegating token refresh to list of
 * defined providers until it finds provider which supports passed token. If no provider from
 * the list supports the token, {@link IllegalArgumentException} is thrown.
 *
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public class DefaultAuthenticationRefreshManager implements AuthenticationRefreshManager {

    private Set<AuthenticationRefreshProvider> providers = new HashSet<>();

    public DefaultAuthenticationRefreshManager(Collection<? extends AuthenticationRefreshProvider> authProviders) {
        super();
        for (AuthenticationRefreshProvider provider : authProviders) {
            providers.add(provider);
        }
    }

    @Override
    public Authentication refreshAuthentication(Authentication previousAuthentication) {
        Optional<Authentication> refreshedToken;
        for (AuthenticationRefreshProvider provider : providers) {
            refreshedToken = provider.reauthenticateIfAuthenticationSupported(previousAuthentication);
            if (refreshedToken.isPresent()) {
                return refreshedToken.get();
            }
        }
        //if no provider supports this token, throw exception
        throw new IllegalArgumentException("Authentication token not supported");
    }
}
