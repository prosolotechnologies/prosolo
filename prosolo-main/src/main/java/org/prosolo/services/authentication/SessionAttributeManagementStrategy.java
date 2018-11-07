package org.prosolo.services.authentication;

import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.annotations.SessionAttributeScope;
import org.prosolo.services.authentication.listeners.UserSessionEndStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * Removes session attributes according to {@link SessionAttributeScope} annotation present
 * on session attribute class and {@link org.prosolo.services.authentication.annotations.AuthenticationChangeType} value configured for this
 * strategy. Also, if this authentication strategy is configured with {@link org.prosolo.services.authentication.annotations.AuthenticationChangeType#USER_SESSION_END} value
 * {@link org.prosolo.services.authentication.listeners.UserSessionEndStrategy#onUserSessionEnd(HttpSession)}
 * is invoked.
 *
 * @author stefanvuckovic
 * @date 2018-11-02
 * @since 1.2.0
 */
public class SessionAttributeManagementStrategy implements SessionAuthenticationStrategy {

    private UserSessionEndStrategy sessionEndStrategy;
    private AuthenticationChangeType authenticationChangeType;

    public SessionAttributeManagementStrategy(UserSessionEndStrategy userSessionEndStrategy, AuthenticationChangeType authenticationChangeType) {
        this.sessionEndStrategy = userSessionEndStrategy;
        this.authenticationChangeType = authenticationChangeType;
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (authenticationChangeType == AuthenticationChangeType.USER_SESSION_END) {
                sessionEndStrategy.onUserSessionEnd(session);
            }
            Enumeration sessionAttributeNames = session.getAttributeNames();
            while (sessionAttributeNames.hasMoreElements()) {
                String key = (String) sessionAttributeNames.nextElement();
                Object attr = session.getAttribute(key);
                if (attr.getClass().isAnnotationPresent(SessionAttributeScope.class)) {
                    SessionAttributeScope annotation = attr.getClass().getAnnotation(SessionAttributeScope.class);
                    if (authenticationChangeType.getValue() >= annotation.end().getValue()) {
                        session.removeAttribute(key);
                    }
                }
            }
        }
    }
}
