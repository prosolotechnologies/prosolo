package org.prosolo.services.authentication.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.nodes.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * Spring-security requires an implementation of UserDetailService. 
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static Logger logger = Logger.getLogger(UserDetailsServiceImpl.class);

	@Autowired private UserManager userManager;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String defaultEmail) throws UsernameNotFoundException {
		logger.debug("Loading user details for the user: " + defaultEmail);
		org.prosolo.common.domainmodel.user.User user = userManager.getUser(defaultEmail);

		if (user == null)
			throw new UsernameNotFoundException("There is no user registered with this email.");

		Collection<SimpleGrantedAuthority> userAuthorities = new ArrayList<SimpleGrantedAuthority>();

		for (Role role : user.getRoles()) {
			userAuthorities.add(new SimpleGrantedAuthority(addRolePrefix(role.getTitle())));
		}
		
//		Collection<Unit_User> userUnit = user.getUnitUser();
//
//		for (Unit_User unit_User : userUnit) {
//			Collection<Unit_User_Role> unitUserRole = unit_User.getUnitUserRole();
//
//			for (Unit_User_Role unit_User_Role : unitUserRole) {
//				Role userRole = unit_User_Role.getRole();
//				userAuthorities.add(new SimpleGrantedAuthority(addRolePrefix(userRole.getTitle())));
//			}
//		}

		boolean enabled = true;
		boolean accountNonExpired = true;
		boolean credentialsNonExpired = true;
		boolean accountNonLocked = true;
		
		logger.debug("Returnign user details " + defaultEmail +", user.getPassword(): "+ 
				user.getPassword()+", enabled: "+ enabled+", accountNonExpired: "+
				accountNonExpired+", credentialsNonExpired: "+credentialsNonExpired+", accountNonLocked: "+ 
				accountNonLocked+", userAuthorities: "+userAuthorities);

		return new User(defaultEmail, user.getPassword(), enabled,
				accountNonExpired, credentialsNonExpired, accountNonLocked,
				userAuthorities);
	}

	private String addRolePrefix(String role) {
		return "ROLE_" + role.toUpperCase();
	}

}
