package org.prosolo.services.authentication.listeners;

import javax.servlet.http.HttpSession;

/**
 * Performs actions needed when user session ends like generating event for session end, data/file cleanup.
 *
 * @author stefanvuckovic
 * @date 2018-11-06
 * @since 1.2.0
 */
public interface UserSessionEndStrategy {

    /**
     * Should be called when user session ends to perform logic needed in this case
     *
     * @param session - should not be marked as invalid before this method is called
     */
    void onUserSessionEnd(HttpSession session);
}
