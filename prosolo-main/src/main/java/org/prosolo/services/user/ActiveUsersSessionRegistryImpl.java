package org.prosolo.services.user;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Uses {@link org.springframework.security.web.session.HttpSessionEventPublisher} to remove user sessions
 * so HttpSessionEventPublisher must be registered in web.xml for this class to work
 *
 * @author stefanvuckovic
 * @date 2018-11-09
 * @since 1.2.0
 */
@Service("org.prosolo.services.user.ActiveUsersSessionRegistry")
public class ActiveUsersSessionRegistryImpl implements ActiveUsersSessionRegistry, ApplicationListener<HttpSessionDestroyedEvent> {

    private static Logger logger = Logger.getLogger(ActiveUsersSessionRegistryImpl.class);

    private final ConcurrentMap<Long, Set<HttpSession>> activeUsers;
    private final ConcurrentMap<HttpSession, Long> activeSessions;

    public ActiveUsersSessionRegistryImpl() {
        this.activeUsers = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    @Override
    public void registerUserSession(long userId, HttpSession session) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than zero");
        }

        if (session == null) {
            throw new IllegalArgumentException("session can't be null");
        }

        logger.debug("Registering session " + session.getId() + ", for user " + userId);

        if (isSessionRegistered(session)) {
            /*
            if session is already registered remove it. This is important because some other user
            may have this session assigned to him.
             */
            removeSession(session);
        }

        activeSessions.put(session, userId);

        Set<HttpSession> userSessions = activeUsers.get(userId);
        if (userSessions == null) {
            userSessions = new CopyOnWriteArraySet<>();
            Set<HttpSession> previousUserSesssions = activeUsers.putIfAbsent(userId, userSessions);
            if (previousUserSesssions != null) {
                userSessions = previousUserSesssions;
            }
        }
        userSessions.add(session);

        logger.debug("Session " + session.getId() + " registered for user " + userId);
    }

    @Override
    public Set<HttpSession> getAllUserSessions(long userId) {
        Set<HttpSession> userSessions = activeUsers.get(userId);
        return userSessions != null ? userSessions : Collections.emptySet();
    }

    @Override
    public long getNumberOfActiveSessions() {
        return activeSessions.size();
    }

    @Override
    public void onApplicationEvent(HttpSessionDestroyedEvent httpSessionDestroyedEvent) {
         removeSession(httpSessionDestroyedEvent.getSession());
    }

    private void removeSession(HttpSession session) {
        long userId = getUserIdForSessionIfExists(session);

        if (userId == 0) {
            return;
        }

        activeSessions.remove(session);
        logger.debug("Removed session " + session.getId() + " from active sessions collection");

        Set<HttpSession> userSessions = activeUsers.get(userId);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session);

        if (userSessions.isEmpty()) {
            // user can be removed since he has no active sessions
            activeUsers.remove(userId);
            logger.debug("User: " + userId + " removed from registry since he has no active sessions");
        }
    }

    private boolean isSessionRegistered(HttpSession session) {
        return activeSessions.containsKey(session);
    }

    private long getUserIdForSessionIfExists(HttpSession session) {
        return isSessionRegistered(session) ? activeSessions.get(session) : 0;
    }
}
