package org.prosolo.core.spring.security.successhandlers;

import org.prosolo.core.spring.security.HomePageResolver;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SessionDataInitializerSuccessHandler {

	@Override
	public void determineSuccessTargetUrl(HttpServletRequest request, Authentication authentication, Map<String, Object> sessionData) {
		if (authentication instanceof RememberMeAuthenticationToken) {
			String uri = request.getRequestURI() +
					(request.getQueryString() != null ? "?" + request.getQueryString() : "");
			uri = uri.substring(request.getContextPath().length());
			logger.info("Remember me login; URL: " + uri);
			setDefaultTargetUrl(uri);
		} else {
			Long orgId = (Long) sessionData.get("organizationId");
			long organizationId = orgId == null ? 0 : orgId;
			String url = new HomePageResolver().getHomeUrl(organizationId);
			logger.info("Standard login; URL: " + url);
			setDefaultTargetUrl(url);
		}
	}

}