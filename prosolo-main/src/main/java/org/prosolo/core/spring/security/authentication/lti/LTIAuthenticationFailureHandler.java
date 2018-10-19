package org.prosolo.core.spring.security.authentication.lti;

import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.Util;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Failure handler for LTI authentication that redirects user to the url specified by external system (LTI consumer)
 * or if this url is not specified, to the default target url
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
public class LTIAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String url = request.getParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
        if (url != null) {
            String returnURL = buildReturnURL(url);
            logger.info("LTI authentication failure url " + returnURL);
            saveException(request, exception);
            if (isUseForward()) {
                logger.info("Forwarding to " + returnURL);
                request.getRequestDispatcher(returnURL).forward(request, response);
            } else {
                logger.info("Redirecting to " + returnURL);
                getRedirectStrategy().sendRedirect(request, response, returnURL);
            }
        } else {
            logger.info("LTI consumer did not send return url to redirect user back");
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    // create return url with query params
    private String buildReturnURL(String url) {
        Map<String, String> params = new HashMap<>();
        params.put(LTIConstants.PARAM_LTI_ERRORLOG, "Activity can not be started");
        params.put(LTIConstants.PARAM_LTI_ERRORMSG, "Activity can not be started!");
        return Util.buildURLWithParams(url, params);
    }
}
