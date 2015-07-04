/**
 * 
 */
package org.prosolo.services.email.generators;

import java.io.IOException;
import java.util.Calendar;

import org.prosolo.app.Settings;


/**
 * @author "Nikola Milikic"
 *
 */
public abstract class EmailContentGenerator {
	
	static final String templateHTMLRoot = "src/main/resources/org/prosolo/web/email/html/";
	static final String templateTextRoot = "src/main/resources/org/prosolo/web/email/text/";
	
	abstract String getTemplateName();

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
		return Settings.getInstance().config.emailNotifier.smtpConfig.fullEmail;
	}
	
	public String getDomain() {
		return Settings.getInstance().config.application.domain;
	}
	
	public String getYear() {
		return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	}
}
