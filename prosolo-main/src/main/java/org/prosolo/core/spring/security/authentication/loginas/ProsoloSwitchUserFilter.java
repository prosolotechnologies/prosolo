package org.prosolo.core.spring.security.authentication.loginas;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author stefanvuckovic
 * @date 2018-10-22
 * @since 1.2.0
 */
public class ProsoloSwitchUserFilter extends SwitchUserFilter {

    private SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private AuthenticationSuccessHandler authSuccessHandler;
    private AuthenticationFailureHandler authFailureHandler;

    public ProsoloSwitchUserFilter(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        super();
        //when user logs in as another user, his session should not be ended
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    @Override
    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        super.setSuccessHandler(successHandler);
        this.authSuccessHandler = successHandler;
    }

    @Override
    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        super.setFailureHandler(failureHandler);
        this.authFailureHandler = failureHandler;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // check for switch or exit request
        if (requiresSwitchUser(request)) {
            loginAs(request, response);
            return;
        } else if (requiresExitUser(request)) {
            exitLoginAs(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void loginAs(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // if set, attempt switch and store original
        try {
            Authentication targetUser = attemptSwitchUser(request);
            sessionAuthenticationStrategy.onAuthentication(targetUser, request, response);
            // update the current context to the new target user
            SecurityContextHolder.getContext().setAuthentication(targetUser);

            // redirect to target url
            authSuccessHandler.onAuthenticationSuccess(request, response, targetUser);
        } catch (AuthenticationException e) {
            logger.debug("Switch User failed", e);
            authFailureHandler.onAuthenticationFailure(request, response, e);
        }

        return;
    }

    private void exitLoginAs(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // get the original authentication object (if exists)
        Authentication originalUser = attemptExitUser(request);
        sessionAuthenticationStrategy.onAuthentication(originalUser, request, response);
        // update the current context back to the original user
        SecurityContextHolder.getContext().setAuthentication(originalUser);

        // redirect to target url
        authSuccessHandler.onAuthenticationSuccess(request, response, originalUser);
    }
}
