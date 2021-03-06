package org.prosolo.common.config;

import org.simpleframework.xml.Element;

public class EmailNotifierConfig {

	@Element(name = "activated")
	public boolean activated = false;
	
	@Element(name = "daily")
	public boolean daily = false;
	
	@Element(name = "weekly")
	public boolean weekly = false;
	
	@Element(name = "monthly")
	public boolean monthly = false;

	@Element(name="bcc")
	public boolean bcc =false;

	@Element(name="bcc-email")
	public String bccEmail;
	
	@Element(name = "smtp-config")
	public SMTPConfig smtpConfig;
}
