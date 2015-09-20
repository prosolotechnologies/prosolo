package org.prosolo.services.email;

import static org.junit.Assert.*;

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

import org.junit.Test;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.SMTPConfig;
import org.prosolo.services.email.generators.EmailVerificationEmailContentGenerator;

/**
@author Zoran Jeremic May 22, 2015
 *
 */

public class EmailSenderTest {

	@Test
	public void testSendEmail() {
		EmailVerificationEmailContentGenerator contentGenerator = new EmailVerificationEmailContentGenerator("Test", "http://example.com");
		String email="zoran.jeremic@gmail.com"; 
		String subject= "Verify email1";
		//final String FROM = "zoran.jeremic@gmail.com"; 
		  SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
			
			String host = smtpConfig.host;
			String user = smtpConfig.user;
			String pass = smtpConfig.pass;
			String from=smtpConfig.fullEmail;
		    
		    // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
		    // STARTTLS to encrypt the connection.
		    final int PORT = 587;
		    
		    
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
		try {
			message.setFrom(new InternetAddress(from));
			//for (String to : strings) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
		//}
		
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
		System.out.println("FINISHED SENDING MESSAGE");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	@Test
	public void testAmazonSES(){
		final String FROM = "zoran.jeremic@gmail.com";   // Replace with your "From" address. This address must be verified.
	   final String TO = "zoran.jeremic@gmail.com";  // Replace with a "To" address. If your account is still in the 
	                                                       // sandbox, this address must be verified.
	    
	    final String BODY = "This email was sent through the Amazon SES SMTP interface by using Java.";
	    final String SUBJECT = "Amazon SES test (SMTP interface accessed using Java)";
	    
	    // Supply your SMTP credentials below. Note that your SMTP credentials are different from your AWS credentials.
	  // final String SMTP_USERNAME = "AKIAINKX6ZZIXRMUOLQQ";  // Replace with your SMTP username.
	    //final String SMTP_PASSWORD = "Aj7I5cNTT/dMel7rlfXxHc6Zc0ipxQsQrUihj2lY9uPj";  // Replace with your SMTP password.
	    
	    // Amazon SES SMTP host name. This example uses the us-west-2 region.
	   // final String HOST = "email-smtp.us-east-1.amazonaws.com";   
	    
	    SMTPConfig smtpConfig = CommonSettings.getInstance().config.emailNotifier.smtpConfig;
		
		String host = smtpConfig.host;
		String user = smtpConfig.user;
		String pass = smtpConfig.pass;
	    
	    // Port we will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
	    // STARTTLS to encrypt the connection.
	    final int PORT = 587;
	    
	    
	    // Create a Properties object to contain connection configuration information.
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", PORT); 
    	props.put("mail.smtp.auth", "true");
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties. 
    	Session session = Session.getDefaultInstance(props);
        // Create a message with the specified information. 
        MimeMessage msg = new MimeMessage(session);
         try
        {
        	  msg.setFrom(new InternetAddress(FROM));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
			 msg.setSubject(SUBJECT);
		        msg.setContent(BODY,"text/plain");
		            
		        // Create a transport.        
		        Transport transport = session.getTransport();
 
       
                    
        // Send the message.
     
            System.out.println("Attempting to send an email through the Amazon SES SMTP interface...");
            
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(host, user, pass);
        	
            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
            transport.close(); 
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {
            // Close and terminate the connection.
                 	
        }
    }


}

