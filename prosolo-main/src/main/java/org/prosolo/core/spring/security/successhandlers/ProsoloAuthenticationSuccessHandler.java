package org.prosolo.core.spring.security.successhandlers;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.event.EventFactory;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.SessionCountBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Custom authentication success handler that generates login event for authenticated user and
 * allows implementing classes to provide logic for determining success url
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
public abstract class ProsoloAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Inject private EventFactory eventFactory;
    @Inject private ApplicationBean applicationBean;
    @Inject private SessionCountBean sessionCounter;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        ProsoloUserDetails user = (ProsoloUserDetails) authentication.getPrincipal();
        UserContextData context = UserContextData.of(user.getUserId(),
                user.getOrganizationId(), user.getSessionId(), user.getIpAddress(), new PageContextData());
        eventFactory.generateEvent(EventType.LOGIN, context, null, null, null, null);
        registerNewUserSession(user.getUserId(), request.getSession());
        determineSuccessTargetUrl(request, authentication);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void registerNewUserSession(long userId, HttpSession session) {
        applicationBean.registerNewUserSession(userId, session);
        sessionCounter.addSession(session.getId());
    }

    protected abstract void determineSuccessTargetUrl(HttpServletRequest request, Authentication authentication);

}
