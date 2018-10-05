package org.prosolo.core.spring.security.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.AuthenticationSuccessSessionInitializer;
import org.prosolo.core.spring.security.UserSessionDataLoader;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.impl.CredentialManagerImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2018-09-17
 * @since 1.2.0
 */
@Service("org.prosolo.core.spring.security.AuthenticationSuccessSessionInitializer")
public class AuthenticationSuccessSessionInitializerImpl implements AuthenticationSuccessSessionInitializer {

    private static Logger logger = Logger.getLogger(AuthenticationSuccessSessionInitializerImpl.class);

    @Inject private UserSessionDataLoader sessionDataLoader;
    @Inject private EventFactory eventFactory;

    @Override
    public Map<String, Object> initUserSessionData(HttpServletRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        HttpSession session = request.getSession(true);
        Map<String, Object> sessionData = null;
        try {
            sessionData = sessionDataLoader.init(user.getUsername(), request, session);
            session.setAttribute("user", sessionData);
            try {
                UserContextData context = UserContextData.of((long) sessionData.get("userId"),
                        (long) sessionData.get("organizationId"), (String) sessionData.get("sessionId"),
                        new PageContextData());
                eventFactory.generateEvent(EventType.LOGIN, context, null, null, null, null);
            } catch (Exception e) {
                logger.error(e);
            }
            return sessionData;
        } catch (SessionInitializationException e) {
            logger.error("Error", e);
            return null;
        }
    }
}
