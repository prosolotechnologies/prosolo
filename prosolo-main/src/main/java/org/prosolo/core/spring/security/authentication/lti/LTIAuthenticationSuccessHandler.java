package org.prosolo.core.spring.security.authentication.lti;

import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.core.spring.security.authentication.lti.authenticationtoken.LTIAuthenticationToken;
import org.prosolo.core.spring.security.authentication.lti.urlbuilder.ToolLaunchUrlBuilderFactory;
import org.prosolo.core.spring.security.successhandlers.ProsoloAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Success handler for LTI authentication that initializes user session data and redirects user to the target url
 *
 * @author stefanvuckovic
 * @date 2018-10-16
 * @since 1.2.0
 */
@Component
public class LTIAuthenticationSuccessHandler extends ProsoloAuthenticationSuccessHandler {

    @Override
    protected void determineSuccessTargetUrl(HttpServletRequest request, Authentication authentication) {
        LtiTool ltiTool = ((LTIAuthenticationToken) authentication).getPreauthenticationToken().getLtiTool();
        setDefaultTargetUrl(ToolLaunchUrlBuilderFactory.getLaunchUrlBuilder(ltiTool).getLaunchUrl());
    }
}
