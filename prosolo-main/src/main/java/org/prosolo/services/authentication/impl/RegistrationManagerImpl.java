package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.app.RegistrationKey;
import org.prosolo.domainmodel.user.Email;
import org.prosolo.domainmodel.user.OpenIDAccount;
import org.prosolo.services.authentication.RegistrationManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-10-25
 *
 */
@Service("org.prosolo.services.authentication.RegistrationManager")
public class RegistrationManagerImpl extends AbstractManagerImpl implements RegistrationManager {
 
	private static final long serialVersionUID = -94874019425008552L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RegistrationManager.class);

	@Override
	@Transactional
	public boolean isEmailAlreadyExists(String emailAddress) {
		String query = 
				"SELECT count(email) " + 
				"FROM Email email " + 
				"WHERE email.address = :emailAddress ";
		
		Long emailCount = (Long) persistence.currentManager().createQuery(query)
				.setString("emailAddress", emailAddress)
				.uniqueResult();
		
		if (emailCount != null) {
			return emailCount > 0;
		}
		return false;
	}
	
	@Override
	@Transactional
	public Email getEmailByVerificationKey(String verificationKey){
		String query = 
			"SELECT email " +
			"FROM Email email " +
			"WHERE email.verificationKey = :verificationKey " ;
	
		Email email = (Email) persistence.currentManager().createQuery(query).
				setString("verificationKey", verificationKey).
				uniqueResult();
		return email;
	}
	
	@Override
	@Transactional
	public boolean setEmailAsVerified(String emailAddress,boolean verified){
		String query = 
			"SELECT email " +
			"FROM Email email " +
			"WHERE email.address = :emailAddress " ;
		
		Email email = (Email) persistence.currentManager().createQuery(query).
				setString("emailAddress", emailAddress).
				uniqueResult();
		
		if (email != null) {
			email.setVerified(verified);
			this.saveEntity(email);
			return true;
		}
		return false;
	}
	
	@Override
	@Transactional
	public RegistrationKey getRegistrationKeyById(String registrationKey){
		String query = 
			"SELECT regKey " +
			"FROM RegistrationKey regKey " +
			"WHERE regKey.uid = :uid " ;
		
		return (RegistrationKey) persistence.currentManager().createQuery(query).
				setString("uid", registrationKey).
				uniqueResult();
	}
	@Override
	@Transactional
	public OpenIDAccount findOpenIDAccount(String validatedId){
		String query = 
				"SELECT openIdAccount " +
				"FROM OpenIDAccount openIdAccount " +
				"WHERE openIdAccount.validatedId = :validatedId " ;
		OpenIDAccount openIDAccount=(OpenIDAccount) persistence.currentManager().createQuery(query).
		setString("validatedId", validatedId).uniqueResult();
		return openIDAccount;
	}

}
