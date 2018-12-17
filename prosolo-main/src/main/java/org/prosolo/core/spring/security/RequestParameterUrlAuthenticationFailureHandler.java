package org.prosolo.core.spring.security;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Tries to send user to failure url sent in request when authentication failure occurs
 *
 * @author stefanvuckovic
 * @date 2018-10-26
 * @since 1.2.0
 */
public abstract class RequestParameterUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String url = getFailureUrlFromRequest(request);
        if (url != null) {
            logger.info("Authentication failure url from request " + url);
            saveException(request, exception);
            if (isUseForward()) {
                logger.info("Forwarding to " + url);
                request.getRequestDispatcher(url).forward(request, response);
            } else {
                logger.info("Redirecting to " + url);
                getRedirectStrategy().sendRedirect(request, response, url);
            }
        } else {
            logger.info("Failure url not sent in request");
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    protected abstract String getFailureUrlFromRequest(HttpServletRequest request);
}
