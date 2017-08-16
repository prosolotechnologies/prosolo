package org.prosolo.services.authentication.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.nodes.RoleManager;
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
	@Inject private RoleManager roleManager;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		logger.debug("Loading user details for the user: " + email);
		org.prosolo.common.domainmodel.user.User user = userManager.getUser(email);

		if (user == null)
			throw new UsernameNotFoundException("There is no user registered with this email.");

		Collection<SimpleGrantedAuthority> userAuthorities = new ArrayList<SimpleGrantedAuthority>();

		for (Role role : user.getRoles()) {
			List<String> capabilities = roleManager.getNamesOfRoleCapabilities(role.getId());
			if(capabilities != null){
				for(String cap:capabilities){
					userAuthorities.add(new SimpleGrantedAuthority(cap.toUpperCase()));
				}
			}
		}

		boolean enabled = true;
		boolean accountNonExpired = true;
		boolean credentialsNonExpired = true;
		boolean accountNonLocked = true;
		
		logger.debug("Returning user details " + email +", user.getPassword(): "+ 
				user.getPassword()+", enabled: "+ enabled+", accountNonExpired: "+
				accountNonExpired+", credentialsNonExpired: "+credentialsNonExpired+", accountNonLocked: "+ 
				accountNonLocked+", userAuthorities: "+userAuthorities);
		String password=user.getPassword();
				if(password==null){
					password="";
				}
		return new User(email, password, enabled,
				accountNonExpired, credentialsNonExpired, accountNonLocked,
				userAuthorities);
	}

}
