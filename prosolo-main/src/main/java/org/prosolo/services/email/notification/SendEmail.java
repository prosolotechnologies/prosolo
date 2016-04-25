package org.prosolo.services.email.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.SMTPConfig;
import org.prosolo.common.domainmodel.user.User;

public class SendEmail {

	public void sendEmailToUser(User user, String html, String subject) throws AddressException, MessagingException {
		sendEmail(html, user.getEmail(), subject);
	}

	public void sendEmail(String html, String email, String subject) throws AddressException, MessagingException {
 
		SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
		String host = smtpConfig.host;
		String user = smtpConfig.user;
		String pass = smtpConfig.pass;
		
		Properties props = System.getProperties();
		props.put("mail.smtp.starttls.enable", String.valueOf(smtpConfig.starttlsenable));
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", smtpConfig.port);
		props.put("mail.smtp.auth", String.valueOf(smtpConfig.auth));

		Session session = Session.getDefaultInstance(props, null);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(user));

		Collection<InternetAddress> toAddress = new ArrayList<InternetAddress>();

		//for (String to : emails) {
		//email="zoran.jeremic@gmail.com";
		if(Settings.getInstance().config.application.developmentMode){
			email=Settings.getInstance().config.application.developmentEmail;
		}
			toAddress.add(new InternetAddress(email));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		//}

		message.setSubject(subject);
		message.setText("Test message");
		 	message.setDataHandler(new DataHandler(new HTMLDataSource(html)));

		Transport transport = session.getTransport("smtp");
		transport.connect(host, user, pass);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}
	
}
