package org.prosolo.core.spring.security;

import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Inject private AuthenticationSuccessSessionInitializer authenticationSuccessSessionInitializer;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		logger.info("CustomAuthenticationSuccessHandler on Auth Success Start");
		Map<String, Object> sessionData = authenticationSuccessSessionInitializer.initUserSessionData(request, authentication);

		if (sessionData != null) {
			if (authentication instanceof RememberMeAuthenticationToken) {
				String uri = request.getRequestURI() +
						(request.getQueryString() != null ? "?" + request.getQueryString() : "");
				uri = uri.substring(request.getContextPath().length());
				logger.info("Remember me login; URL: " + uri);
				setDefaultTargetUrl(uri);
			} else {
				Long orgId = (Long)sessionData.get("organizationId");
				long organizationId = orgId == null ? 0 : orgId;
				String url = new HomePageResolver().getHomeUrl(organizationId);
				logger.info("Standard login; URL: " + url);
				setDefaultTargetUrl(url);
			}
			super.onAuthenticationSuccess(request, response, authentication);
		} else {
			authentication = null;
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(null);
			setDefaultTargetUrl("/login?error=" + URLEncoder.encode("Wrong email or password! Please try again.", "utf-8"));
			logger.error("Session initialization during login failed");
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}
	
}