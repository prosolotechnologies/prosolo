package org.prosolo.services.authentication;

import org.prosolo.core.spring.security.SpringSecurityConfig;
import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.listeners.UserSessionEndStrategy;
import org.prosolo.services.authentication.reauthentication.AuthenticationRefreshManager;
import org.prosolo.services.authentication.reauthentication.DefaultAuthenticationRefreshManager;
import org.prosolo.services.authentication.reauthentication.providers.LTIAuthenticationRefreshProvider;
import org.prosolo.services.authentication.reauthentication.providers.RememberMeAuthenticationRefreshProvider;
import org.prosolo.services.authentication.reauthentication.providers.SAMLAuthenticationRefreshProvider;
import org.prosolo.services.authentication.reauthentication.providers.UsernamePasswordAuthenticationRefreshProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Bean definitions for authentication related functionalities
 *
 * @author stefanvuckovic
 * @date 2018-11-06
 * @since 1.2.0
 */
@Configuration
public class AuthenticationConfig {

    @Inject private UserDetailsService userDetailsService;
    @Inject private UserSessionEndStrategy userSessionEndStrategy;

    @Bean
    public AuthenticationRefreshManager authenticationRefreshManager() {
        return new DefaultAuthenticationRefreshManager(
                Arrays.asList(
                        new UsernamePasswordAuthenticationRefreshProvider(userDetailsService),
                        new SAMLAuthenticationRefreshProvider(userDetailsService),
                        new LTIAuthenticationRefreshProvider(userDetailsService),
                        new RememberMeAuthenticationRefreshProvider(SpringSecurityConfig.REMEMBER_ME_KEY, userDetailsService)
                ));
    }

    @Bean
    @Scope("prototype")
    public SessionAttributeManagementStrategy sessionAttributeManagementStrategy(AuthenticationChangeType authenticationChangeType) {
        return new SessionAttributeManagementStrategy(userSessionEndStrategy, authenticationChangeType);
    }

    @Bean
    public Function<AuthenticationChangeType, SessionAttributeManagementStrategy> sessionAttributeManagementStrategyFactory() {
        return type -> sessionAttributeManagementStrategy(type);
    }
}
