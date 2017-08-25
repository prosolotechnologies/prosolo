package org.prosolo.core.spring.security;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.services.event.EventFactory;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

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
				UserContextData context = UserContextData.of((long) sessionData.get("userId"),
						(long) sessionData.get("organizationId"), (String) sessionData.get("sessionId"),
						new LearningContextData());
				eventFactory.generateEvent(EventType.LOGIN, context, null, null, null, null);
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
				setDefaultTargetUrl(uri);
			} else {
				setDefaultTargetUrl(new HomePageResolver().getHomeUrl());
			}
			super.onAuthenticationSuccess(request, response, authentication);
		} else {
			authentication = null;
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(null);
			setDefaultTargetUrl("/login?error=" + URLEncoder.encode("Wrong email or password! Please try again.", "utf-8"));
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}
	
}