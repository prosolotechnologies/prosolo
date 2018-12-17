package org.prosolo.core.spring.security.authentication.lti;

import org.prosolo.core.spring.security.RequestParameterUrlAuthenticationFailureHandler;
import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.web.lti.Util;

import javax.servlet.http.HttpServletRequest;
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
public class LTIAuthenticationFailureHandler extends RequestParameterUrlAuthenticationFailureHandler {

    @Override
    protected String getFailureUrlFromRequest(HttpServletRequest request) {
        String url = request.getParameter(LTIConstants.LAUNCH_PRESENTATION_RETURN_URL);
        return url != null ? buildReturnURL(url) : null;
    }

    // create return url with query params
    private String buildReturnURL(String url) {
        Map<String, String> params = new HashMap<>();
        params.put(LTIConstants.PARAM_LTI_ERRORLOG, "Activity can not be started");
        params.put(LTIConstants.PARAM_LTI_ERRORMSG, "Activity can not be started!");
        return Util.buildURLWithParams(url, params);
    }
}
