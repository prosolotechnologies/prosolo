package org.prosolo.core.spring.security.authentication.lti;

import com.google.gson.Gson;
import org.prosolo.common.domainmodel.lti.LtiVersion;
import org.prosolo.core.spring.security.authentication.lti.authenticationtoken.LTIAuthenticationToken;
import org.prosolo.core.spring.security.authentication.lti.util.LTIConstants;
import org.prosolo.services.lti.LtiToolLaunchValidator;
import org.prosolo.services.lti.LtiToolManager;
import org.prosolo.services.lti.data.LTIToolData;
import org.prosolo.web.lti.message.LTILaunchMessage;
import org.prosolo.web.lti.message.extract.LtiMessageBuilder;
import org.prosolo.web.lti.message.extract.LtiMessageBuilderFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Authenticates user through LTI Provider component when lti activity is started from the
 * external system.
 *
 * If user was previously logged in, his session is terminated and new one is created.
 *
 * Note: If GET request to lti launch url is sent, nothing will be done, user would not be
 * authenticated and old user session would not be terminated (if existed). In other words,
 * request would not be processed by this filter.
 *
 * @author stefanvuckovic
 * @date 2018-10-15
 * @since 1.2.0
 */
public class LTIAuthenticationFilterImpl extends AbstractAuthenticationProcessingFilter implements LTIAuthenticationFilter {

    @Inject private LtiToolManager toolManager;
    @Inject private LtiToolLaunchValidator toolLaunchValidator;

    private final static String DEFAULT_PROCESSING_URL = "/ltiproviderlaunch.xhtml";
    private Set<LogoutHandler> logoutHandlers = new HashSet<>();

    public LTIAuthenticationFilterImpl(LTIAuthenticationProvider authenticationProvider, LTIAuthenticationSuccessHandler successHandler, SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        super(new AntPathRequestMatcher(DEFAULT_PROCESSING_URL, "POST"));
        setAuthenticationManager(new ProviderManager(Arrays.asList(authenticationProvider)));
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(new LTIAuthenticationFailureHandler());
        setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
    }

    public void setDefaultAuthenticationFailureUrl(String url) {
        ((LTIAuthenticationFailureHandler) getFailureHandler()).setDefaultFailureUrl(url);
    }

    @Override
    public void addLogoutHandler(LogoutHandler handler) {
        this.logoutHandlers.add(handler);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        //make sure to proceed with authentication only if request method is valid
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!requiresAuthentication(request, response) || !isRequestMethodValid(request)) {
            chain.doFilter(req, res);
            return;
        }
        super.doFilter(req, res, chain);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            logger.info("New Lti Launch");
            /*
            if there was existing session user is logged out by calling logout handlers that are configured and then authenticated
             */
            logoutPreviouslyLoggedInUser(request, response);
            LTILaunchMessage msg = createLTILaunchMessage(request);
            LTIToolData ltiTool = validateToolLaunchAndReturnLtiTool(msg, request);
            return this.getAuthenticationManager().authenticate(LTIAuthenticationToken.createPreauthenticationToken(msg, ltiTool));
        } catch (AuthenticationServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("error", e);
            throw new AuthenticationServiceException("LTI authentication error");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        ((LTIAuthenticationToken) authResult).authenticationCompleted();
    }

    private void logoutPreviouslyLoggedInUser(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for (LogoutHandler logoutHandler : logoutHandlers) {
            logoutHandler.logout(request, response, authentication);
        }
    }

    private boolean isRequestMethodValid(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        if (!LTIConstants.POST_REQUEST.equalsIgnoreCase(httpMethod)) {
            logger.error("LTI PROVIDER LAUNCH METHOD NOT POST AS EXPECTED BUT: " + httpMethod);
            return false;
        }
        logger.info("Launch request valid");
        return true;
    }

    // wrap POST parameters in LTILaunchMessage object
    private LTILaunchMessage createLTILaunchMessage(HttpServletRequest request) throws Exception {
        try {
            LtiMessageBuilder msgE = LtiMessageBuilderFactory.createMessageExtractor(request);
            LTILaunchMessage msg = (LTILaunchMessage) msgE.getLtiMessage(request);
            return msg;
        } catch (Exception e) {
            throw new Exception("Required parameters missing from launch or not valid", e);
        }

    }

    private LTIToolData validateToolLaunchAndReturnLtiTool(LTILaunchMessage msg, HttpServletRequest request) {
        logger.info("LTI provider launch: " + new Gson().toJson(msg));
        LTIToolData tool = toolManager.getToolDetailsData(msg.getId());
        // validating the tool, the method will throw an exception if not valid
        toolLaunchValidator.validateLaunch(tool, msg.getConsumerKey(), getVersion(msg.getLtiVersion()), request);
        logger.info("Tool launch valid, tool id: " + tool.getId());
        return tool;
    }

    private LtiVersion getVersion(String version){
        LtiVersion vers = null;
        if (LTIConstants.LTI_VERSION_ONE.equals(version)) {
            vers = LtiVersion.V1;
        } else {
            if (LTIConstants.LTI_VERSION_TWO.equals(version)) {
                vers = LtiVersion.V2;
            }
        }
        return vers;
    }

}
