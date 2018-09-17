package org.prosolo.core.spring.security;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2018-09-17
 * @since 1.2.0
 */
public interface AuthenticationSuccessSessionInitializer {

    Map<String, Object> initUserSessionData(HttpServletRequest request, Authentication authentication);
}
