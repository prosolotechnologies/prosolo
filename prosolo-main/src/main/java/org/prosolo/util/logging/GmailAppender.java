package org.prosolo.util.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.log4j.net.SMTPAppender;
import org.prosolo.app.Settings;

/**
 * @author Zoran Jeremic Sep 19, 2014
 *
 */

public class GmailAppender extends SMTPAppender {
	private boolean startTLS = false;
	private String ipAddress=null;

	@Override
	protected Session createSession() {
		Properties props = null;
		try {
			props = new Properties(System.getProperties());
		} catch (SecurityException ex) {
			props = new Properties();
		}
		String prefix = "mail.smtp";
		if (this.getSMTPProtocol() != null) {
			props.put("mail.transport.protocol", this.getSMTPProtocol());
			prefix = "mail." + this.getSMTPProtocol();
		}

		if (this.getSMTPHost() != null)
			props.put(prefix + ".host", this.getSMTPHost());
		if (this.getSMTPPort() > 0)
			props.put(prefix + ".port", String.valueOf(this.getSMTPPort()));
		if (this.startTLS)
			props.put("mail.smtp.starttls.enable", "true");

		Authenticator auth = null;
		if (this.getSMTPPassword() != null && this.getSMTPUsername() != null) {
			props.put(prefix + ".auth", "true");
			auth = new Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							GmailAppender.this.getSMTPUsername(),
							GmailAppender.this.getSMTPPassword());
				}

			};
		}
		Session session = Session.getInstance(props, auth);
		if (this.getSMTPProtocol() != null)
			session.setProtocolForAddress("rfc822", this.getSMTPProtocol());
		if (this.getSMTPDebug())
			session.setDebug(this.getSMTPDebug());
		return session;
	}

	public boolean isStartTLS() {
		return this.startTLS;
	}

	public void setStartTLS(boolean startTLS) {
		this.startTLS = startTLS;
	}
 	@Override
 	public void setSubject(String subject){
		  try {			  
	 			this.ipAddress=InetAddress.getLocalHost().getHostAddress();
		  		} catch (UnknownHostException e) {	 
			e.printStackTrace();	 
		  }
 		super.setSubject(subject+" BC["+Settings.getInstance().config.init.bcName+"]["+this.ipAddress+"]");
 	}
 }
