package org.prosolo.services.authentication.reauthentication.providers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;

/**
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public class SAMLAuthenticationRefreshProvider extends AuthenticationRefreshProvider {

    public SAMLAuthenticationRefreshProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    @Override
    protected Authentication reauthenticate(Authentication previousAuthentication, UserDetails refreshedUserDetails) {
        ExpiringUsernameAuthenticationToken previousAuth = (ExpiringUsernameAuthenticationToken) previousAuthentication;
        ExpiringUsernameAuthenticationToken newAuth = new ExpiringUsernameAuthenticationToken(previousAuth.getTokenExpiration(), refreshedUserDetails, previousAuth.getCredentials(), previousAuth.getAuthorities());
        newAuth.setDetails(refreshedUserDetails);
        return newAuth;
    }

    @Override
    protected boolean supports(Class<? extends Authentication> aClass) {
        return aClass.equals(ExpiringUsernameAuthenticationToken.class);
    }
}
