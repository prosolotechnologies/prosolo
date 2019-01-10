package org.prosolo.services.user;

import javax.servlet.http.HttpSession;
import java.util.Set;

/**
 * Keeps track of active users and their sessions
 *
 * @author stefanvuckovic
 * @date 2018-11-09
 * @since 1.2.0
 */
public interface ActiveUsersSessionRegistry {

    /**
     * Registers user session. If same session was previously assigned to another user it is
     * unassigned from that user before it is registered again with the new user.
     *
     * Should be called when user authenticates successfully
     *
     * @param userId
     * @param session
     */
    void registerUserSession(long userId, HttpSession session);

    /**
     * Returns all sessions for a given user
     *
     * @param userId
     * @return
     */
    Set<HttpSession> getAllUserSessions(long userId);

    /**
     * Returns number of currently active sessions
     *
     * @return
     */
    long getNumberOfActiveSessions();
}
