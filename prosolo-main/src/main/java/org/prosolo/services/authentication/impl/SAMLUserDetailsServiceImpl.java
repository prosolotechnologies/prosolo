/*
 * Copyright 2016 Vincenzo De Notaris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.prosolo.services.authentication.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {
	
	private static Logger logger = Logger.getLogger(SAMLUserDetailsServiceImpl.class);
	
	@Inject 
	private UserDetailsService userDetailsService;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UserManager userManager;
	
	@Override
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {
		try {
			String email = credential.getNameID().getValue();
			try {
				//try to log in as regular user
				return userDetailsService.loadUserByUsername(email);
			} catch(UsernameNotFoundException e) {
				//if email does not exist, create new user account;
				Role role = roleManager.getRoleByName("User");
				List<Long> roles = new ArrayList<>();
				roles.add(role.getId());
				String firstname = credential.getAttributeAsString("firstName");
				String lastname = credential.getAttributeAsString("lastName");
				String fName = firstname != null && !firstname.isEmpty() ? firstname : "Name";
				String lName = lastname != null && !lastname.isEmpty() ? lastname : "Lastname";
				
				org.prosolo.common.domainmodel.user.User user = userManager.createNewUser(fName, 
						lName, email, true, UUID.randomUUID().toString(), null, null, null, roles);
				
				logger.info("NEW USER THROUGH SAML WITH EMAIL " + email + " is logged in");
				
				Collection<SimpleGrantedAuthority> userAuthorities = new ArrayList<SimpleGrantedAuthority>();
				List<String> capabilities = roleManager.getNamesOfRoleCapabilities(role.getId());
				//userAuthorities.add(new SimpleGrantedAuthority(addRolePrefix(role.getTitle())));
				if(capabilities != null){
					for(String cap:capabilities){
						userAuthorities.add(new SimpleGrantedAuthority(cap.toUpperCase()));
					}
				}

				return new User(email, user.getPassword(), true, true, true, true, userAuthorities);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
}
