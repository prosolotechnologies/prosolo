package org.prosolo.core.spring.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Inject
	private UserSessionDataLoader sessionDataLoader;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		User user = (User) authentication.getPrincipal();
		HttpSession session = request.getSession(true);
		
		boolean success;
		try{
			Map<String, Object> sessionData = sessionDataLoader.init(user.getUsername(), request, session);
			session.setAttribute("user", sessionData);
			success = true;
		}catch(SessionInitializationException e){
			success = false;
		}
		//session.setAttribute("email", user.getUsername());
		//boolean success = loggedUserBean.login(user.getUsername(), user.getPassword(), request, session);
		if (success) {
			if(authentication instanceof RememberMeAuthenticationToken){
				String uri = request.getRequestURI();
				uri = uri.substring(request.getContextPath().length());
				String url = request.getRequestURL().toString();
				
				setDefaultTargetUrl(uri);
			}else{
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
