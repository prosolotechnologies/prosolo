package org.prosolo.services.authentication.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OpenIDAccount;
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
	public User getUserByVerificationKey(String verificationKey){
		String query = 
			"SELECT user " +
			"FROM User user " +
			"WHERE user.verificationKey = :verificationKey " ;
	
		User email = (User) persistence.currentManager().createQuery(query).
				setString("verificationKey", verificationKey).
				uniqueResult();
		return email;
	}
	
	@Override
	@Transactional
	public boolean setUserAsVerified(String email, boolean verified){
		String query = 
			"SELECT user " +
			"FROM User user " +
			"WHERE user.email = :email " ;
		
		User user = (User) persistence.currentManager().createQuery(query).
				setString("email", email).
				uniqueResult();
		
		if (user != null) {
			user.setVerified(verified);
			this.saveEntity(user);
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
