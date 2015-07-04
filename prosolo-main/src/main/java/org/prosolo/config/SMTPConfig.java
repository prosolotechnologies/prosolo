package org.prosolo.config;

import org.simpleframework.xml.Element;

public class SMTPConfig {

	@Element(name = "host")
	public String host;
	
	@Element(name = "user")
	public String user;
	
	@Element(name = "full-email")
	public String fullEmail;
	
	@Element(name = "pass")
	public String pass;
	
	@Element(name = "port")
	public int port;
	
	@Element(name = "starttls-enable", required = false)
	public boolean starttlsenable = false;
	
	@Element(name = "auth", required = false)
	public boolean auth = false;
}
