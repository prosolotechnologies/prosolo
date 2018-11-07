package org.prosolo.services.authentication.reauthentication.providers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public class UsernamePasswordAuthenticationRefreshProvider extends AuthenticationRefreshProvider {

    public UsernamePasswordAuthenticationRefreshProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    @Override
    protected Authentication reauthenticate(Authentication previousAuthentication, UserDetails refreshedUserDetails) {
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(refreshedUserDetails, previousAuthentication.getCredentials(), previousAuthentication.getAuthorities());
        newAuth.setDetails(previousAuthentication.getDetails());
        return newAuth;
    }

    @Override
    protected boolean supports(Class<? extends Authentication> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
