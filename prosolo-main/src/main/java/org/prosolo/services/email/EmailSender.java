package org.prosolo.services.email;

import org.prosolo.common.config.AppConfig;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.SMTPConfig;
import org.prosolo.common.email.generators.EmailContentGenerator;
import org.prosolo.common.email.generators.EmailVerificationEmailContentGenerator;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service("org.prosolo.services.email.EmailSender")
public class EmailSender {
	
	public boolean sendEmail(EmailContentGenerator contentGenerator, String email) throws AddressException, MessagingException, FileNotFoundException, IOException {
		// if development mode is ON, comments should be sent only to the developer
		if (CommonSettings.getInstance().config.appConfig.projectMode.equals(AppConfig.ProjectMode.DEV)) {
			email = CommonSettings.getInstance().config.appConfig.developerEmail;
		}

		SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
		
		String host = smtpConfig.host;
		String user = smtpConfig.user;
		String pass = smtpConfig.pass;
		String from = smtpConfig.fullEmail;
		    
	    // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
	    // STARTTLS to encrypt the connection.
//	    final int PORT = 25;
		    
		    
	    // Create a Properties object to contain connection configuration information.
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", smtpConfig.port); 
    	props.put("mail.smtp.auth", "true");
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(props);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from, "ProSolo"));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

		if (CommonSettings.getInstance().config.emailNotifier.bcc)
    		message.addRecipient(Message.RecipientType.BCC, new InternetAddress(CommonSettings.getInstance().config.emailNotifier.bccEmail));

		message.setSubject(contentGenerator.getSubject());
		
		// Unformatted text version
		final MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(contentGenerator.generatePlainText(), "text/plain");
      
		// HTML version
		final MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(contentGenerator.generateHTML(), "text/html");
		
		// Create the Multipart. Add BodyParts to it.
		final Multipart mp = new MimeMultipart("alternative");
		mp.addBodyPart(textPart);
		mp.addBodyPart(htmlPart);
		
		// Set Multipart as the message's content
		message.setContent(mp);
		
		Transport transport = session.getTransport("smtp");
		transport.connect(host, user, pass);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
		
		return true;
	}
 
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			EmailVerificationEmailContentGenerator contentGenerator = new EmailVerificationEmailContentGenerator("Nik", "http://example.com");
			new EmailSender().sendEmail(contentGenerator,  "test@prosolo.ca");
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
