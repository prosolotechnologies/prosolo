package org.prosolo.core.spring.security;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is invoked when a non-authenticated user has requested a restricted resource. In this case, we check
 * whether it is an AJAX request, in which case we pass the correct XML that the servlet container expects in order
 * to do the forwarding to the login page properly.
 *
 * @author stefanvuckovic
 * @date 2018-07-12
 * @since 1.2.0
 */
public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static Logger logger = Logger.getLogger(CustomLoginUrlAuthenticationEntryPoint.class);

    private static final String AJAX_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<partial-response><redirect url=\"%s\"></redirect></partial-response>";

    public CustomLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            logger.info("Non authenticated user detected through ajax request; redirect to login page");
            // Redirect on ajax request requires special XML response. See also http://stackoverflow.com/a/9311920/157882
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().printf(AJAX_REDIRECT_XML, request.getContextPath() + getLoginFormUrl());
        } else {
            logger.info("Non authenticated user detected through standard, non ajax request; redirect to login page");
            super.commence(request, response, authException);
        }
    }
}
