package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.security.successhandlers.DefaultProsoloAuthenticationSuccessHandler;
import org.prosolo.services.authentication.AuthenticationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Service("org.prosolo.services.authentication.AuthenticationService")
public class SpringSecurityAuthenticationServiceImpl implements AuthenticationService, Serializable {
	
	private static final long serialVersionUID = 221859676151839996L;

	private static Logger logger = Logger.getLogger(SpringSecurityAuthenticationServiceImpl.class);

	@Inject
	private PasswordEncoder passwordEncoder;
	@Inject
	private DefaultProsoloAuthenticationSuccessHandler authSuccessHandler;
	@Inject
	private UserDetailsService userDetailsService;

	@Override
	public boolean loginUserOpenID(String email) {
		try {
			Authentication authentication = authenticateUser(email);
			return authentication.isAuthenticated();
		} catch (org.springframework.security.core.AuthenticationException e) {
			logger.error("Error", e);
			return false;
		}
	}

	private Authentication authenticateUser(String email) {
		email = email.toLowerCase();
		logger.debug("log in user with email: " + email);
		UserDetails user = userDetailsService.loadUserByUsername(email);
		Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
				user.getAuthorities());
		logger.debug("Authentication token created for:" + email);

		//SecurityContextHolder.getContext().setAuthentication(authentication);
		if (authentication.isAuthenticated()) {
			logger.info("Authentication was successful");
			SecurityContextHolder.getContext().setAuthentication(authentication);
//			HttpSession session = request.getSession(true);
//			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		} else {
			logger.info("Authentication was not successful");
		}
		return authentication;
	}

	@Override
	public void logout() {
		// tell to Spring Security that user is not authorized any more
		SecurityContextHolder.getContext().setAuthentication(null);
		SecurityContextHolder.clearContext();
		
		// invalidate JSF session
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

		final HttpServletRequest request = (HttpServletRequest) ec.getRequest();
		request.getSession(false).invalidate();
	}

	@Override
	public boolean checkPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
