package org.prosolo.bigdata.email;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
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

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.SMTPConfig;
import org.prosolo.common.email.generators.EmailContentGenerator;
import org.prosolo.common.email.generators.EmailVerificationEmailContentGenerator;

 
 
 

/**
 * @author Zoran Jeremic, Sep 19, 2015
 *
 */
public class EmailSender {
	private static Logger logger = Logger
			.getLogger(EmailSender.class.getName());
	public void sendBatchEmails(Map<EmailContentGenerator,String> emailsToSend) throws AddressException{
		logger.info("SEND BATCH EMAILS");
		SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
		Session session = Session.getDefaultInstance(getMailProperties());

		try{
			Transport transport = session.getTransport("smtp");
			transport.connect(smtpConfig.host, smtpConfig.user, smtpConfig.pass);
		for(Map.Entry<EmailContentGenerator,String> emailToSend:emailsToSend.entrySet()){
			String email=emailToSend.getValue();
			EmailContentGenerator contentGenerator=emailToSend.getKey();
				logger.info("sending email:"+email);
				Multipart mp=createMailMultipart(contentGenerator);
				Message message=createMessage(email, contentGenerator.getSubject(),session, mp);
				transport.sendMessage(message, message.getAllRecipients());


		}
			transport.close();
		}catch(MessagingException me){
			me.printStackTrace();
		}catch (IOException ioe){
			ioe.printStackTrace();
		}




	}
	public void sendEmail(EmailContentGenerator contentGenerator, String email) throws AddressException, MessagingException, FileNotFoundException, IOException {
		SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;


        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(getMailProperties());
		Multipart mp=createMailMultipart(contentGenerator);
		Message message=createMessage(email, contentGenerator.getSubject(),session, mp);


		try{

			Transport transport = session.getTransport("smtp");
			transport.connect(smtpConfig.host, smtpConfig.user, smtpConfig.pass);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private Multipart createMailMultipart(EmailContentGenerator contentGenerator) throws IOException, MessagingException{
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

		return mp;
	}
	private Message createMessage(String email, String subject, Session session, Multipart mp) throws AddressException, MessagingException{
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(CommonSettings.getInstance().config.emailNotifier.smtpConfig.fullEmail));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

		message.setSubject(subject);
		// Set Multipart as the message's content
		message.setContent(mp);
		return message;
	}
	private Properties getMailProperties(){


		// Create a Properties object to contain connection configuration information.
		Properties props = System.getProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", CommonSettings.getInstance().config.emailNotifier.smtpConfig.port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.starttls.required", "true");
		return props;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			EmailVerificationEmailContentGenerator contentGenerator = new EmailVerificationEmailContentGenerator("Nik", "http://example.com");
			new EmailSender().sendEmail(contentGenerator,  "example@gmail.com");
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
 
 
}
