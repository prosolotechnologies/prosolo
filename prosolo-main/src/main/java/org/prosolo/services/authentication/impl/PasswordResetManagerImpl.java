/**
 * 
 */
package org.prosolo.services.authentication.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.app.ResetKey;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.authentication.exceptions.ResetKeyDoesNotExistException;
import org.prosolo.services.authentication.exceptions.ResetKeyExpiredException;
import org.prosolo.services.authentication.exceptions.ResetKeyInvalidatedException;
import org.prosolo.services.email.generators.PasswordResetEmailContentGenerator;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.authentication.PasswordResetManager")
public class PasswordResetManagerImpl extends AbstractManagerImpl implements PasswordResetManager {
	
	private static final long serialVersionUID = 7929317962497049673L;
	
	private static Logger logger = Logger.getLogger(PasswordResetManager.class);
	
	@Autowired private EmailSender emailSender;

	@Override
	@Transactional(readOnly = false)
	public boolean initiatePasswordReset(User user, String email, String serverAddress) {
		return initiatePasswordReset(user,email,serverAddress,persistence.currentManager());
	}

	@Override
	public boolean initiatePasswordReset(User user, String email, String serverAddress, Session session) {
		email = email.toLowerCase();

		// first invalidate all other user's request key
		invalidateUserRequestKeys(user,session);

		ResetKey key = new ResetKey();
		key.setUser(user);
		key.setDateCreated(new Date());
		key.setUid(UUID.randomUUID().toString().replace("-", ""));
		saveEntity(key,session);

		try {
			String resetAddress = serverAddress + "/" + key.getUid();
			PasswordResetEmailContentGenerator contentGenerator = new PasswordResetEmailContentGenerator(user.getName(), resetAddress);
			emailSender.sendEmail(contentGenerator,  email);
			return true;
		} catch (AddressException e) {
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return false;
	}

	@Override
	public boolean checkIfResetKeyIsValid(String resetKey) throws ResetKeyDoesNotExistException, ResetKeyInvalidatedException, ResetKeyExpiredException {
		ResetKey result = getResetKey(resetKey);
		
		if (result != null) {
			if (result.isInvalid()) {
				throw new ResetKeyInvalidatedException("Reset key: "+ resetKey +" is not valid.");
			}
			
			boolean keyExpired = DateUtil.hoursBetween(result.getDateCreated(), new Date()) > Settings.getInstance().config.application.passwordResetKeyValidityHours;
			
			if (keyExpired) {
				throw new ResetKeyExpiredException("Reset key: "+ resetKey +" has expired.");
			}
			
			return true;
		} else {
			throw new ResetKeyDoesNotExistException("Reset key: "+ resetKey +" does not exist.");
		}
	}
	
	@Override
	public User getResetKeyUser(String resetKey) {
		ResetKey key = getResetKey(resetKey);
		
		return key.getUser();
	}

	public ResetKey getResetKey(String resetKey) {
		Session session = persistence.currentManager();
		
		String query = 
			"SELECT resetKey " +
			"FROM ResetKey resetKey " +
			"WHERE resetKey.uid = :resetKey ";
		
		ResetKey result = (ResetKey) session.createQuery(query).
				setString("resetKey", resetKey).
				uniqueResult();
		return result;
	}
	
	public void invalidateUserRequestKeys(User user,Session session) {

		String query = 
			"SELECT resetKey " +
			"FROM ResetKey resetKey " +
			"WHERE resetKey.user = :user " +
			"AND resetKey.invalid = :invalid";
		
		@SuppressWarnings("unchecked")
		List<ResetKey> keys = session.createQuery(query)
				.setEntity("user", user)
				.setBoolean("invalid", false)
				.list();
		
		if (keys != null && !keys.isEmpty()) {
			for (ResetKey resetKey : keys) {
				resetKey.setInvalid(true);
				saveEntity(resetKey, session);
			}
		}
	}
	
	@Override
	public void invalidateResetKey(String resetKey) {
		ResetKey resetK = getResetKey(resetKey);
		
		if (resetK != null) {
			resetK.setInvalid(true);
			saveEntity(resetK);
		}
	}
}
