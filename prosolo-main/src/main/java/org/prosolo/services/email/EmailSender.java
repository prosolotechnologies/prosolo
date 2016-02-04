package org.prosolo.services.email;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.SMTPConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.email.generators.EmailContentGenerator;
import org.prosolo.common.email.generators.EmailVerificationEmailContentGenerator;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.email.EmailSender")
public class EmailSender {

	public void sendEmailToUser(User user, EmailContentGenerator html, String subject) throws AddressException, MessagingException, FileNotFoundException, IOException {
		sendEmail(html,  user.getEmail().getAddress(), subject);
	}
	
	public void sendEmail(EmailContentGenerator contentGenerator, String email, String subject) throws AddressException, MessagingException, FileNotFoundException, IOException {
		SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
		
		String host = smtpConfig.host;
		String user = smtpConfig.user;
		String pass = smtpConfig.pass;
		String from = smtpConfig.fullEmail;
		    
	    // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
	    // STARTTLS to encrypt the connection.
	    final int PORT = 25;
		    
		    
	    // Create a Properties object to contain connection configuration information.
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", PORT); 
    	props.put("mail.smtp.auth", "true");
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(props);
		
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		
		message.setSubject(subject);
		
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
	}
 
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			EmailVerificationEmailContentGenerator contentGenerator = new EmailVerificationEmailContentGenerator("Nik", "http://example.com");
			new EmailSender().sendEmail(contentGenerator,  "zoran.jeremic@gmail.com" , "Verify email1");
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
