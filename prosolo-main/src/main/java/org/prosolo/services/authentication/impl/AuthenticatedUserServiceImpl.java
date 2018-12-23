package org.prosolo.services.authentication.impl;

import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.authentication.reauthentication.AuthenticationRefreshManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for returning information about user authentication: user details, checking if user is logged in and so on
 *
 *
 * @author stefanvuckovic
 * @date 2018-10-30
 * @since 1.2.0
 */
@Service ("org.prosolo.services.authentication.AuthenticatedUserService")
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    @Inject private AuthenticationRefreshManager authRefreshManager;

    @Override
    public boolean isUserLoggedIn() {
        return getAuthenticationOfALoggedInUser(false).isPresent();
    }

    @Override
    public Optional<ProsoloUserDetails> getLoggedInUser() {
        Optional<Authentication> authenticationOpt = getAuthenticationOfALoggedInUser(false);
        return authenticationOpt.isPresent()
                ? Optional.of((ProsoloUserDetails) authenticationOpt.get().getPrincipal())
                : Optional.empty();
    }

    @Override
    public Optional<Authentication> getUserAuthentication() {
        return getAuthenticationOfALoggedInUser(true);
    }

    /**
     * Method should be called from context where user details are guaranteed to be available
     * without HttpSession object passed.
     *
     * @param returnIfAnonymous
     * @return
     */
    private Optional<Authentication> getAuthenticationOfALoggedInUser(boolean returnIfAnonymous) {
        SecurityContext context = SecurityContextHolder.getContext();
        return getAuthenticationFromSecurityContext(context, returnIfAnonymous);
    }

    private Optional<Authentication> getAuthenticationFromSecurityContext(SecurityContext context, boolean returnIfAnonymous) {
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        //if authentication is instance of AnonymousAuthenticationToken it means user is not really logged in
        if (!returnIfAnonymous && authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    @Override
    public boolean doesUserHaveCapability(String capability) {
        Optional<Authentication> auth = getUserAuthentication();

        if (!auth.isPresent()) {
            return false;
        }

        List<GrantedAuthority> capabilities = (List<GrantedAuthority>) auth.get().getAuthorities();

        if (capabilities == null) {
            return false;
        }

        for (GrantedAuthority ga : capabilities) {
            if (ga.getAuthority().toUpperCase().equals(capability.toUpperCase())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<ProsoloUserDetails> getCurrentlyLoggedInUser(HttpSession session) {
        /*
        first try to get user data from current security context spring is holding in security context holder
        and if that is not possible (authentication is null) retrieve it from session.

        This is because session may not contain up to date authentication data if spring security filter chain has not
        finished execution and storing up to date authentication back in session
         */
        Optional<Authentication> auth = getUserAuthentication();
        if (auth.isPresent()) {
            return (auth.get() instanceof AnonymousAuthenticationToken) ? Optional.empty() : Optional.of((ProsoloUserDetails) auth.get().getPrincipal());
        } else {
            return getUserDetailsFromSession(session);
        }
    }

    @Override
    public Optional<ProsoloUserDetails> getUserDetailsFromSession(HttpSession session) {
        SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null) {
            return Optional.empty();
        }
        Optional<Authentication> auth = getAuthenticationFromSecurityContext(securityContext, false);
        return auth.isPresent()
                ? Optional.of((ProsoloUserDetails) auth.get().getPrincipal())
                : Optional.empty();
    }

    @Override
    public void refreshUserSessionData() {
        Optional<Authentication> auth = getAuthenticationOfALoggedInUser(false);
        if (auth.isPresent()) {
            Authentication newAuth = authRefreshManager.refreshAuthentication(auth.get());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }
}
