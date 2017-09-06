package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.services.authentication.UserAuthenticationService;
import org.prosolo.services.nodes.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/*
 * Spring-security requires an implementation of UserDetailService. 
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static Logger logger = Logger.getLogger(UserDetailsServiceImpl.class);

	@Autowired private UserManager userManager;
	@Inject private UserAuthenticationService authService;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		logger.debug("Loading user details for the user: " + email);
		org.prosolo.common.domainmodel.user.User user = userManager.getUser(email);

		if (user == null) {
			throw new UsernameNotFoundException("There is no user with this email");
		}

		return authService.authenticateUser(user);
	}

}
