package org.prosolo.services.authentication.reauthentication.providers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-11-01
 * @since 1.2.0
 */
public abstract class AuthenticationRefreshProvider {

    private UserDetailsService userDetailsService;

    public AuthenticationRefreshProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public Optional<Authentication> reauthenticateIfAuthenticationSupported(Authentication previousAuthentication) throws UsernameNotFoundException {
        if (supports(previousAuthentication.getClass())) {
            return Optional.ofNullable(reauthenticate(previousAuthentication));
        }
        return Optional.empty();
    }

    private Authentication reauthenticate(Authentication previousAuthentication) throws UsernameNotFoundException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(((UserDetails) previousAuthentication.getPrincipal()).getUsername());
        return reauthenticate(previousAuthentication, userDetails);
    }

    protected abstract Authentication reauthenticate(Authentication authentication, UserDetails refreshedUserDetails);
    protected abstract boolean supports(Class<? extends Authentication> aClass);
}
