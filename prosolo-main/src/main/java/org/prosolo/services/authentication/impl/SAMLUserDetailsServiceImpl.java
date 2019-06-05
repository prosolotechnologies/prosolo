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
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.config.app.SAMLIdentityProviderInfo;
import org.prosolo.services.authentication.UserAuthenticationService;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {

		try {
			logger.info("Authentication through SAML requested; Remote entity id: " + credential.getRemoteEntityID() + "; SAML Credential Name Id: " + credential.getNameID());
			//Gson g=new Gson();
			//System.out.println("LOAD USER BY SAML:"+g.toJson(credential));
			String nameId = null;
			if (credential.getNameID() != null) {
				nameId = credential.getNameID().getValue();
			}
			logger.debug("NameID:" + nameId);
			List<Attribute> attributes = credential.getAttributes();
			for (Attribute attribute : attributes) {
				logger.debug("SAML attribute: " + attribute.getName() + " friendly name: " + attribute.getFriendlyName());
				for (XMLObject value : attribute.getAttributeValues()) {
					logger.debug("has value: " + getStringValueFromXmlObject(value));
				}
			}

			SAMLIdentityProviderInfo provider = getIdentityProviderUsedForAuthentication(credential.getRemoteEntityID());
            logger.info("Identity provider that issued authentication: " + provider.getEntityId());
			String email = credential.getAttributeAsString(provider.emailAttribute);
			String firstname = credential.getAttributeAsString(provider.firstNameAttribute);
			String lastname = credential.getAttributeAsString(provider.lastNameAttribute);
			logger.info("SAML RETURNED:email:" + email + " firstname:" + firstname + " lastname:" + lastname + " nameID:" + credential.getNameID().getValue());

			//try to log in as regular user
			User user = userManager.getUser(email);

			if (user == null) {
				logger.info("User with email: " + email + " does not exist");
				if (provider.createAccountForNonexistentUser) {
				    logger.info("New account for user with email: " + email + " will be created");
                    //if email does not exist, create new user account;
                    Role role = roleManager.getRoleByName(SystemRoleNames.USER);
                    String fName = firstname != null && !firstname.isEmpty() ? firstname : "Name";
                    String lName = lastname != null && !lastname.isEmpty() ? lastname : "Lastname";

                    user = userManager.createNewUser(1, fName,
                            lName, email, true, UUID.randomUUID().toString(), null, null, null, Arrays.asList(role.getId()), false);

                    logger.info("NEW USER THROUGH SAML WITH EMAIL " + email + " is logged in");
                }
			} else {
			    logger.info("Existing user with email " + email + " is logged in through SAML");
            }
			return authService.authenticateUser(user);
		} catch (LockedException|UsernameNotFoundException e) {
			throw e;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new UsernameNotFoundException("Error occurred while logging. Please try again.");
		}
	}

	private String getStringValueFromXmlObject(XMLObject xmlObj) {
		if (xmlObj instanceof XSString) {
			return ((XSString) xmlObj).getValue();
		} else if (xmlObj instanceof XSAny) {
			return ((XSAny) xmlObj).getTextContent();
		} else {
			return null;
		}
	}

    private SAMLIdentityProviderInfo getIdentityProviderUsedForAuthentication(String entityId) {
        List<SAMLIdentityProviderInfo> samlProviders = Settings.getInstance().config.application.registration.samlConfig.samlProviders;
        return samlProviders.stream().filter(p -> p.getEntityId().equals(entityId)).findFirst().get();
    }

}
