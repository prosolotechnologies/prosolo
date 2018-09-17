package org.prosolo.core.spring.security;

import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class LTICustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Inject
	private AuthenticationSuccessSessionInitializer authenticationSuccessSessionInitializer;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		logger.info("LTICustomAuthenticationSuccessHandler on Auth Success Start");
		Map<String, Object> sessionData = authenticationSuccessSessionInitializer.initUserSessionData(request, authentication);

		if (sessionData == null) {
			logger.error("Error initializing the user session data after successful authentication");
			throw new AuthenticationException("Error initializing user session data");
		}
	}

}