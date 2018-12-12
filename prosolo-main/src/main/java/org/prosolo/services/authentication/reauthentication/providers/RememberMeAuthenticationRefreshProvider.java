package org.prosolo.services.authentication.reauthentication.providers;

import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public class RememberMeAuthenticationRefreshProvider extends AuthenticationRefreshProvider {

    private String key;

    public RememberMeAuthenticationRefreshProvider(String key, UserDetailsService userDetailsService) {
        super(userDetailsService);
        this.key = key;
    }

    @Override
    protected Authentication reauthenticate(Authentication previousAuthentication, UserDetails refreshedUserDetails) {
        RememberMeAuthenticationToken previousAuth = (RememberMeAuthenticationToken) previousAuthentication;
        RememberMeAuthenticationToken newAuth = new RememberMeAuthenticationToken(key, refreshedUserDetails, previousAuth.getAuthorities());
        newAuth.setDetails(previousAuth.getDetails());
        return newAuth;
    }

    @Override
    protected boolean supports(Class<? extends Authentication> aClass) {
        return aClass.equals(RememberMeAuthenticationToken.class);
    }
}
