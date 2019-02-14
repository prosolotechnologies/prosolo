package org.prosolo.services.authentication.listeners;

import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Listener that reacts on session destroyed event right before session is invalidated (by session timeout,
 * user logout or by manually invalidating session and invokes logic which should be performed when user session ends
 *
 * @author stefanvuckovic
 * @date 2018-11-06
 * @since 1.2.0
 */
@Component
public class SessionEndListener implements ApplicationListener<HttpSessionDestroyedEvent> {

    @Inject private UserSessionEndStrategy sessionEndStrategy;

    @Override
    public void onApplicationEvent(HttpSessionDestroyedEvent httpSessionDestroyedEvent) {
        sessionEndStrategy.onUserSessionEnd(httpSessionDestroyedEvent.getSession());
    }
}
