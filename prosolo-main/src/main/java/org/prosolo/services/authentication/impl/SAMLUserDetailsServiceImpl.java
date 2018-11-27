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

import org.apache.log4j.Logger;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.authentication.UserAuthenticationService;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

	private static Logger logger = Logger.getLogger(SAMLUserDetailsServiceImpl.class);

	@Inject
	private UserManager userManager;
	@Inject
	private UnitManager unitManager;
	@Inject private RoleManager roleManager;
	@Inject private UserAuthenticationService authService;

	@Override
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {

		try {
			logger.info("Authentication through SAML requested; SAML Credential Name Id: " + credential.getNameID());
			//Gson g=new Gson();
			//System.out.println("LOAD USER BY SAML:"+g.toJson(credential));
			List<Attribute> attributes = credential.getAttributes();
			System.out.println("ATTRIBUTES FOUND:" + attributes.size());
			//System.out.println("ADDITIONAL DATA FOUND:"+credential.getAdditionalData().toString());
			//System.out.println("ADD:"+g.toJson(credential.getAdditionalData()));
			for (Attribute attribute : attributes) {
				logger.info("SAML attribute:" + attribute.getName() + " friendly name:" + attribute.getFriendlyName());
				//logger.info("ATTR:"+g.toJson(attribute));
				for (XMLObject value : attribute.getAttributeValues()) {
					logger.info("has value:" + ((XSString) value).getValue());
					//logger.info("ATTR:"+g.toJson(value));

				}
			}
			String email = credential.getNameID().getValue();
			//String email = credential.getAttributeAsString("email");
			//String eduPersonPrincipalName=credential.getAttributeAsString("eduPersonPrincipalName");
//			String email=credential.getAttributeAsString("urn:oid:0.9.2342.19200300.100.1.3");//should be email attribute
//			if(email==null || email.length()<5){
//				//dirty hack as temporary solution since UTA is not providing emails for test accounts as email attribute, but as eduPersonPrincipalName
//				email = credential.getAttributeAsString("urn:oid:1.3.6.1.4.1.5923.1.1.1.6");//eduPersonPrincipalName
//				logger.info("Email is returned as eduPersonPrincipalName:" + email);
//			}

			String firstname = credential.getAttributeAsString("urn:oid:2.5.4.42");
			String lastname = credential.getAttributeAsString("urn:oid:2.5.4.4");
			logger.info("SAML RETURNED:email:" + email + " firstname:" + firstname + " lastname:" + lastname + " nameID:" + credential.getNameID().getValue());

			//try to log in as regular user
			User user = userManager.getUser(email);

			if (user == null) {
				logger.info("User with email: " + email + " does not exist so new account will be created");
				//if email does not exist, create new user account;
				Role role = roleManager.getRoleByName(SystemRoleNames.USER);
				//String firstname = credential.getAttributeAsString("givenName");
				//String lastname = credential.getAttributeAsString("sn");
				String fName = firstname != null && !firstname.isEmpty() ? firstname : "Name";
				String lName = lastname != null && !lastname.isEmpty() ? lastname : "Lastname";

				user = userManager.createNewUser(1, fName,
						lName, email, true, UUID.randomUUID().toString(), null, null, null, Arrays.asList(role.getId()), false);

				//TODO observer refactor migrate to sequential event processing when there is a possibility to test UTA login
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					logger.error("Error", ie);
				}

				unitManager.addUserToUnitWithRole(user.getId(), 1, role.getId(), UserContextData.empty());

				logger.info("NEW USER THROUGH SAML WITH EMAIL " + email + " is logged in");
			}
			//this check is added because for SAML authentication spring does not do these checks
			if (user.isDeleted()) {
				throw new LockedException("User account is locked");
			}
			return authService.authenticateUser(user);
		} catch (LockedException|UsernameNotFoundException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new UsernameNotFoundException("Error occurred while logging. Please try again.");
		}
	}

}
