package org.prosolo.core.spring.security.authentication.loginas;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author stefanvuckovic
 * @date 2018-10-22
 * @since 1.2.0
 */
public class SessionClearingSwitchUserFilter extends SwitchUserFilter {

    private boolean invalidatePreviousSession = true;

    public SessionClearingSwitchUserFilter() {
        super();
    }

    public SessionClearingSwitchUserFilter(boolean invalidatePreviousSession) {
        this.invalidatePreviousSession = invalidatePreviousSession;
    }

    public void setInvalidatePreviousSession(boolean invalidatePreviousSession) {
        this.invalidatePreviousSession = invalidatePreviousSession;
    }

    @Override
    protected Authentication attemptSwitchUser(HttpServletRequest request) throws AuthenticationException {
        Authentication auth = super.attemptSwitchUser(request);
        if (invalidatePreviousSession && requiresSwitchUser(request)) {
            invalidatePreviousSession(request);
        }
        return auth;
    }

    @Override
    protected Authentication attemptExitUser(HttpServletRequest request) throws AuthenticationCredentialsNotFoundException {
        Authentication auth = super.attemptExitUser(request);
        if (invalidatePreviousSession && requiresExitUser(request)) {
            invalidatePreviousSession(request);
        }
        return auth;
    }

    private void invalidatePreviousSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
