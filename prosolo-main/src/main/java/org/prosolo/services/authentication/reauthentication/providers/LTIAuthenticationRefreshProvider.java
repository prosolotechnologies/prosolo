package org.prosolo.services.authentication.reauthentication.providers;

import org.prosolo.core.spring.security.authentication.lti.authenticationtoken.LTIAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public class LTIAuthenticationRefreshProvider extends AuthenticationRefreshProvider {

    public LTIAuthenticationRefreshProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    @Override
    protected Authentication reauthenticate(Authentication previousAuthentication, UserDetails refreshedUserDetails) {
        LTIAuthenticationToken previousAuth = (LTIAuthenticationToken) previousAuthentication;
        return new LTIAuthenticationToken(refreshedUserDetails, previousAuth.getPreauthenticationToken(), previousAuth.getAuthorities());
    }

    @Override
    protected boolean supports(Class<? extends Authentication> aClass) {
        return aClass.equals(LTIAuthenticationToken.class);
    }
}
