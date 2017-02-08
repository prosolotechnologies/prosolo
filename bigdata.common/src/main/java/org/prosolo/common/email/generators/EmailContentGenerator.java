/**
 * 
 */
package org.prosolo.common.email.generators;

import java.io.IOException;
import java.util.Calendar;

import org.prosolo.common.config.CommonSettings;




/**
 * @author "Nikola Milikic"
 *
 */
public abstract class EmailContentGenerator {
	
	static final String templateHTMLRoot = "org/prosolo/web/email/html/";
	static final String templateTextRoot = "org/prosolo/web/email/text/";
	static final String headerImageURL = "http://public.prosolo.ca/email-header.jpg";
	
	public abstract String getTemplateName();

	public abstract String getSubject();

	public String generateHTML() throws IOException {
		return MoustacheUtil.compileTemplate(templateHTMLRoot + getTemplateName() + ".html", getTemplateName() + ".html", this);
	}

	public String generatePlainText() throws IOException {
		return MoustacheUtil.compileTemplate(templateTextRoot + getTemplateName() + ".moustache", getTemplateName() + ".moustache", this);
	}
	
	/*
	 * Getter methods
	 */
	public String getSenderEmail() {
		return CommonSettings.getInstance().config.emailNotifier.smtpConfig.fullEmail;
	}
	
	public String getDomain() {
		return CommonSettings.getInstance().config.appConfig.domain;
	}
	
	public String getSupportEmail() {
		return CommonSettings.getInstance().config.appConfig.supportEmail;
	}
	
	public String getHeaderImageURL() {
		return headerImageURL;
	}
	
	public String getYear() {
		return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	}
}
