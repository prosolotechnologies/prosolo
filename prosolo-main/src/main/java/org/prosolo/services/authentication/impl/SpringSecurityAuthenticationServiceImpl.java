package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.security.CustomAuthenticationSuccessHandler;
import org.prosolo.core.spring.security.LTICustomAuthenticationSuccessHandler;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Service("org.prosolo.services.authentication.AuthenticationService")
public class SpringSecurityAuthenticationServiceImpl implements AuthenticationService, Serializable {
	
	private static final long serialVersionUID = 221859676151839996L;

	private static Logger logger = Logger.getLogger(SpringSecurityAuthenticationServiceImpl.class);

	@Inject
	private PasswordEncoder passwordEncoder;
	@Inject
	private CustomAuthenticationSuccessHandler authSuccessHandler;
	@Inject
	private UserDetailsService userDetailsService;
	@Inject
	private LTICustomAuthenticationSuccessHandler ltiCustomAuthenticationSuccessHandler;
	
	@Override
	public void loginAs(HttpServletRequest req, HttpServletResponse resp, String email)
			throws AuthenticationException {
		try {
			Authentication authentication = authenticateUser(email);
			authSuccessHandler.onAuthenticationSuccess(req, resp, authentication);
		} catch (Exception e) {
			logger.error("Error while trying to login as user with email " + email + ";", e);
			throw new AuthenticationException("Error while trying to authentication user");
		}
	}

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

	@Override
	public boolean loginUserLTI(HttpServletRequest request, HttpServletResponse response, String email) {
		try {
			Authentication authentication = authenticateUser(email);
			if (authentication.isAuthenticated()) {
				ltiCustomAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error", e);
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(null);
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
