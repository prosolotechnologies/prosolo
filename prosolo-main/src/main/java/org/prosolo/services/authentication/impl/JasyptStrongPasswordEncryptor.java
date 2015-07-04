package org.prosolo.services.authentication.impl;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.authentication.PasswordEncrypter")
public class JasyptStrongPasswordEncryptor implements PasswordEncrypter, Serializable {

	private static final long serialVersionUID = 1993925349465267528L;
	
	private static Logger logger = Logger.getLogger(SpringSecurityAuthenticationServiceImpl.class);

	@Override
	public String encodePassword(String password) {
		return encodePassword(password, null);
	}
	
	@Override
	public String encodePassword(String password, Object salt) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String encodedPassword = passwordEncryptor.encryptPassword(password);
		logger.debug("Encrypted password is: "+encodedPassword);
		return encodedPassword;
	}

	@Override
	public boolean isPasswordValid(String encryptedPassword, String plainPassword, Object salt) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		logger.debug("Checking password: "+encryptedPassword);
		boolean checkPassword = passwordEncryptor.checkPassword(plainPassword, encryptedPassword);
		logger.debug("Passwords match: "+checkPassword);
		
		return checkPassword;
	}
}
