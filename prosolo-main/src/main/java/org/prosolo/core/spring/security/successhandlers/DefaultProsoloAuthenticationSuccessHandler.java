package org.prosolo.core.spring.security.successhandlers;

import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class DefaultProsoloAuthenticationSuccessHandler extends ProsoloAuthenticationSuccessHandler {

	@Override
	public void determineSuccessTargetUrl(HttpServletRequest request, Authentication authentication) {
		if (authentication instanceof RememberMeAuthenticationToken) {
			String uri = request.getRequestURI() +
					(request.getQueryString() != null ? "?" + request.getQueryString() : "");
			uri = uri.substring(request.getContextPath().length());
			logger.info("Remember me login; URL: " + uri);
			setDefaultTargetUrl(uri);
		} else {
			ProsoloUserDetails user = (ProsoloUserDetails) authentication.getPrincipal();
			Long orgId = user.getOrganizationId();
			String url = new HomePageResolver().getHomeUrl(orgId);
			logger.info("Standard login; URL: " + url);
			setDefaultTargetUrl(url);
		}
	}

}