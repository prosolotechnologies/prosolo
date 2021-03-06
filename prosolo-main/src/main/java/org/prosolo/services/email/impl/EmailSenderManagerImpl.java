/**
 * 
 */
package org.prosolo.services.email.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.app.ResetKey;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.email.generators.EmailVerificationEmailContentGenerator;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.email.generators.AccountCreatedEmailGenerator;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.email.EmailSenderManager")
public class EmailSenderManagerImpl extends AbstractManagerImpl implements EmailSenderManager {

	private static final long serialVersionUID = 2315279521375014103L;
	
	private static Logger logger = Logger.getLogger(EmailSenderManagerImpl.class);
	
	@Autowired private EmailSender emailSender;

	@Override
	public boolean sendEmailAboutNewAccount(User user, String email) throws IOException {
		return sendEmailAboutNewAccount(user, email, persistence.currentManager());
	}

	@Override
	public boolean sendEmailAboutNewAccount(User user, String email, Session session) throws IOException {
		email = email.toLowerCase();
		
		ResetKey resetKey = new ResetKey();
		resetKey.setUser(user);
		resetKey.setDateCreated(new Date());
		resetKey.setUid(UUID.randomUUID().toString().replace("-", ""));
		saveEntity(resetKey, session);
		
		String serverAddress = CommonSettings.getInstance().config.appConfig.domain + "recovery";
		String resetAddress = serverAddress+"?key="+resetKey.getUid();
		
		AccountCreatedEmailGenerator contentGenerator = new AccountCreatedEmailGenerator(user.getName(), resetAddress);
		
		try {
			emailSender.sendEmail(contentGenerator, email);
			return true;
		} catch (AddressException e) {
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		
		return false;
	}
	
	@Override
	public boolean sendEmailVerificationEmailForNewUser(User user) throws FileNotFoundException, IOException{
		String serverAddress = CommonSettings.getInstance().config.appConfig.domain + "verify";
		String verificationAddress = serverAddress + "?key=" + user.getVerificationKey();
		///String fakeEmail="prosolo.2013@gmail.com"; 
		EmailVerificationEmailContentGenerator contentGenerator = new EmailVerificationEmailContentGenerator(user.getName(), verificationAddress);
		
		try {
			emailSender.sendEmail(contentGenerator, user.getEmail());
			return true;
		} catch (AddressException e) {
			logger.error(e);
		} catch (MessagingException e) {
			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
		
		return false;
	}
	
//	public void sendEmailAboutNewDailyDigest() {
//		emailSender.sendEmail(new FeedsEmailGenerator(loggedUser.getUser().getName(), systemFeed), loggedUser.getUser().getEmail().getAddress(), "Personal Feeds");
//	}

}
