package org.prosolo.core.spring.security.successhandlers;

import org.prosolo.core.spring.security.AuthenticationSuccessSessionInitializer;
import org.prosolo.core.spring.security.HomePageResolver;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
public abstract class SessionDataInitializerSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Inject
    private AuthenticationSuccessSessionInitializer authenticationSuccessSessionInitializer;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        Map<String, Object> sessionData = authenticationSuccessSessionInitializer.initUserSessionData(request, authentication);

        if (sessionData != null) {
            determineSuccessTargetUrl(request, authentication, sessionData);
            super.onAuthenticationSuccess(request, response, authentication);
        } else {
            authentication = null;
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(null);
            setDefaultTargetUrl("/login?error=" + URLEncoder.encode("Error occurred during logging in", "utf-8"));
            logger.error("Session initialization during log in failed");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    protected abstract void determineSuccessTargetUrl(HttpServletRequest request, Authentication authentication, Map<String, Object> sessionData);

}
