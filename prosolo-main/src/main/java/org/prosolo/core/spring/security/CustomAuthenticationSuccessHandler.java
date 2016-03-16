package org.prosolo.core.spring.security;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.services.event.EventFactory;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	
	@Inject
	private UserSessionDataLoader sessionDataLoader;
	@Inject
	private EventFactory eventFactory;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
			
		User user = (User) authentication.getPrincipal();
		HttpSession session = request.getSession(true);
	
		boolean success;
		try {
			Map<String, Object> sessionData = sessionDataLoader.init(user.getUsername(), request, session);
			session.setAttribute("user", sessionData);
			try {
				eventFactory.generateEvent(EventType.LOGIN, (org.prosolo.common.domainmodel.user.User) sessionData.get("user"));
			} catch (Exception e) {
				logger.error(e);
			}
			success = true;
		} catch (SessionInitializationException e) {
			success = false;
		}
		
		if (success) {
			if (authentication instanceof RememberMeAuthenticationToken) {
				String uri = request.getRequestURI() + 
						(request.getQueryString() != null ? "?" + request.getQueryString() : "");
				uri = uri.substring(request.getContextPath().length());
				//uri = uri.startsWith("/") ? uri : "/" + uri;
//				String url = request.getRequestURL().toString();
				
				setDefaultTargetUrl(uri);
			} else {
				setDefaultTargetUrl(new HomePageResolver().getHomeUrl());
			}
			// setAlwaysUseDefaultTargetUrl(true);
			super.onAuthenticationSuccess(request, response, authentication);
		} else {
			authentication = null;
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(null);
			setDefaultTargetUrl("/login?error=Incorrect email or password");
			super.onAuthenticationSuccess(request, response, authentication);
		}
		
	}
	
}