package org.prosolo.core.spring.security.authentication.loginas;

import org.prosolo.core.spring.security.RequestParameterUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Failure handler for Login as authentication that redirects user to the url sent in login as request.
 * If this url is not sent, user is redirected to the default target url and if that url is also empty,
 * 401 error is sent.
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
public class LoginAsAuthenticationFailureHandler extends RequestParameterUrlAuthenticationFailureHandler {

    private String failureUrlParameter = "failureUrl";


    @Override
    protected String getFailureUrlFromRequest(HttpServletRequest request) {
        return request.getParameter(failureUrlParameter);
    }

    public void setFailureUrlParameter(String failureUrlParameter) {
        this.failureUrlParameter = failureUrlParameter;
    }
}
