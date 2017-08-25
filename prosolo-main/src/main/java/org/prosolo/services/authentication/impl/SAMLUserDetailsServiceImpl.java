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
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.util.roles.RoleNames;
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
	@Inject
	private UnitManager unitManager;
	
	@Override
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {

		try {
			//Gson g=new Gson();
			//System.out.println("LOAD USER BY SAML:"+g.toJson(credential));
			List<Attribute> attributes=credential.getAttributes();
			System.out.println("ATTRIBUTES FOUND:"+attributes.size());
			//System.out.println("ADDITIONAL DATA FOUND:"+credential.getAdditionalData().toString());
			//System.out.println("ADD:"+g.toJson(credential.getAdditionalData()));
			for(Attribute attribute: attributes){
				logger.info("SAML attribute:"+attribute.getName()+" friendly name:"+attribute.getFriendlyName());
				//logger.info("ATTR:"+g.toJson(attribute));
				for(XMLObject value: attribute.getAttributeValues()){
					logger.info("has value:"+((XSString)value).getValue());
					//logger.info("ATTR:"+g.toJson(value));

				}
			}
			//String email = credential.getNameID().getValue();
			//String email = credential.getAttributeAsString("email");
			//String eduPersonPrincipalName=credential.getAttributeAsString("eduPersonPrincipalName");
			String email=credential.getAttributeAsString("urn:oid:0.9.2342.19200300.100.1.3");//should be email attribute
			if(email==null || email.length()<5){
				//dirty hack as temporary solution since UTA is not providing emails for test accounts as email attribute, but as eduPersonPrincipalName
				email=credential.getAttributeAsString("urn:oid:1.3.6.1.4.1.5923.1.1.1.6");//eduPersonPrincipalName
				logger.info("Email is returned as eduPersonPrincipalName:"+email);
			}

			String firstname = credential.getAttributeAsString("urn:oid:2.5.4.42");
			String lastname = credential.getAttributeAsString("urn:oid:2.5.4.4");
			logger.info("SAML RETURNED:email:"+email+" firstname:"+firstname+" lastname:"+lastname+" nameID:"+credential.getNameID().getValue());
			try {
				//try to log in as regular user
				return userDetailsService.loadUserByUsername(email);
			} catch(UsernameNotFoundException e) {
				//if email does not exist, create new user account;
				Role role = roleManager.getRoleByName("User");
				List<Long> roles = new ArrayList<>();
				roles.add(role.getId());
				//String firstname = credential.getAttributeAsString("givenName");
				//String lastname = credential.getAttributeAsString("sn");
				String fName = firstname != null && !firstname.isEmpty() ? firstname : "Name";
				String lName = lastname != null && !lastname.isEmpty() ? lastname : "Lastname";


				org.prosolo.common.domainmodel.user.User user = userManager.createNewUser(1, fName,
						lName, email, true, UUID.randomUUID().toString(), null, null, null, roles);

				long roleId = roleManager.getRoleIdsForName(RoleNames.USER).get(0);

				unitManager.addUserToUnitWithRole(user.getId(), 1, roleId, UserContextData.empty());
				
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
