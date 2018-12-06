package org.prosolo.core.spring.security.authentication.lti.authenticationtoken;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Represents authentication token for LTI authentication. Besides principal, it has a reference to
 * {@link LTIPreauthenticationToken} that is used in the process of authentication and when authentication
 * is successfully completed this preauthentication token should be cleared by calling {@link #authenticationCompleted()}
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
public class LTIAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -8503947244103527350L;

    private final Object principal;
    /*
    token that should be used for authentication until the end of successful authentication request after which
    method authenticationCompleted() should be called
     */
    private LTIPreauthenticationToken preauthenticationToken;

    public static LTIAuthenticationToken createPreauthenticationToken(LTILaunchMessage msg, LtiTool tool) {
        LTIAuthenticationToken authenticationToken = new LTIAuthenticationToken();
        authenticationToken.setPreauthenticationToken(new LTIPreauthenticationToken(msg, tool));
        return authenticationToken;
    }

    /**
     * Should be used only when authentication token is being created to perform authentication against it
     *
     */
    private LTIAuthenticationToken() {
        super(null);
        this.principal = null;
        super.setAuthenticated(false);
    }

    public LTIAuthenticationToken(Object principal, LTIPreauthenticationToken preauthenticationToken, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.preauthenticationToken = preauthenticationToken;
        super.setAuthenticated(true);
    }

    public void authenticationCompleted() {
        //clear preauthentication token since it is not needed after authentication is completed
        setPreauthenticationToken(null);
    }

    /**
     * @param isAuthenticated false is only valid value, if true is passed {@link IllegalArgumentException} will be thrown
     * @throws IllegalArgumentException
     */
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Once created you cannot set this token to authenticated. Create a new instance using the constructor which takes a GrantedAuthority list will mark this as authenticated.");
        } else {
            super.setAuthenticated(false);
        }
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    private void setPreauthenticationToken(LTIPreauthenticationToken preauthenticationToken) {
        this.preauthenticationToken = preauthenticationToken;
    }

    public LTIPreauthenticationToken getPreauthenticationToken() {
        return preauthenticationToken;
    }

    public static class LTIPreauthenticationToken {

        private final LTILaunchMessage launchMessage;
        private final LtiTool ltiTool;

        public LTIPreauthenticationToken(LTILaunchMessage launchMessage, LtiTool ltiTool) {
            this.launchMessage = launchMessage;
            this.ltiTool = ltiTool;
        }

        public LTILaunchMessage getLaunchMessage() {
            return launchMessage;
        }

        public LtiTool getLtiTool() {
            return ltiTool;
        }
    }
}
