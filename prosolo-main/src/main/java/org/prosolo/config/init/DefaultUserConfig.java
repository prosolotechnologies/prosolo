package org.prosolo.config.init;

import org.simpleframework.xml.Element;

public class DefaultUserConfig {

	@Element(name = "name")
	public String name;
	
	@Element(name = "lastname")
	public String lastname;
	
	@Element(name = "email")
	public String email;
	
	@Element(name = "pass")
	public String pass;
	
}