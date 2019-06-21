package org.prosolo.core.spring.security.authentication.lti;

import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.Filter;

/**
 * @author stefanvuckovic
 * @date 2019-05-31
 * @since 1.3.2
 */
public interface LTIAuthenticationFilter extends Filter {

    void addLogoutHandler(LogoutHandler handler);
}
