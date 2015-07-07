package org.prosolo.web.unauthorized;


import java.io.FileNotFoundException;
import java.io.IOException;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.app.RegistrationType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic 2013-10-24
 *
 */
@ManagedBean(name = "selfRegistrationBean")
@Component("selfRegistrationBean")
@Scope("view")
public class SelfRegistrationBean {
	
	private static Logger logger = Logger.getLogger(SelfRegistrationBean.class);

	@Autowired private OrganizationManager organizationManager;
	@Autowired private UserManager userManager;
	@Autowired private EmailSenderManager emailSenderManager;
	@Autowired private RegistrationManager registrationManager;
	@Autowired private EventFactory eventFactory;
	
	private String key;
	private boolean keyValid;
	private String verifyKey;
	private boolean verifyKeyValid;
 
	private String name;
	private String lastName;
	private String password;
	private boolean registrationSuccess = false;
	private String email;
	
	// validating honeypot
    private String honeypot;
    private boolean bot;
 
	public boolean isRegistrationSuccess() {
		return registrationSuccess;
	}

	public void setRegistrationSuccess(boolean registrationSuccess) {
		this.registrationSuccess = registrationSuccess;
	}
 
	public boolean isKeyValid() {
		return keyValid;
	}

	public void setKeyValid(boolean keyValid) {
		this.keyValid = keyValid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	//@Transactional
	public void registerUser() {
		this.bot = false;
		
		if (honeypot != null && !honeypot.isEmpty()) {
			this.bot = true;
			return;
		}
		
		Organization org = organizationManager.lookupDefaultOrganization();
		try {
			User user = userManager.createNewUser(
					name, 
					lastName, 
					email,
					false,
					password, 
					org, 
					null);
			
			Email email = user.getEmail();
			emailSenderManager.sendEmailVerificationEmailForNewUser(user, email);
		} catch (UserAlreadyRegisteredException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		this.registrationSuccess = true;
	}
	
	public User registerUserOpenId(String firstName, String lastName, String email){
		System.out.println("register user open id:"+email);
		Organization org = organizationManager.lookupDefaultOrganization();
		User user = null;
		
		try {
			user = userManager.createNewUser(firstName, lastName, email, true, null, org, null);
		} catch (UserAlreadyRegisteredException e) {
			logger.error(e);
		} catch (EventException e) {
			logger.error(e);
		}
		return user; 
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		if (key != null) {
			RegistrationKey regKey = registrationManager.getRegistrationKeyById(key);
			
			if (regKey != null
					&& regKey.getRegistrationType().equals(	RegistrationType.NO_APPROVAL_ACCESS)) {
				this.keyValid = true;
			} else {
				this.keyValid = false;
			}
		}
	}
	
	public String getVerifyKey() {
		return verifyKey;
	}

	public void setVerifyKey(String verifyKey) {
		if (verifyKey != null) {
			Email email = registrationManager.getEmailByVerificationKey(verifyKey);
			
			if (email != null) {
				this.verifyKeyValid = true;
				email.setVerified(true);
				registrationManager.saveEntity(email);
			} else {
				this.verifyKeyValid = false;
			}
		}
	}
	
	public boolean isVerifyKeyValid() {
		return verifyKeyValid;
	}

	public void setVerifyKeyValid(boolean verifyKeyValid) {
		this.verifyKeyValid = verifyKeyValid;
	}

	public String getHoneypot() {
		return honeypot;
	}

	public void setHoneypot(String honeypot) {
		this.honeypot = honeypot;
	}

	public boolean isBot() {
		return bot;
	}
	
}
