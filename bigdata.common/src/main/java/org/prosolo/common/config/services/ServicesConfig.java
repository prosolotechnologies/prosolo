package org.prosolo.common.config.services;

import org.prosolo.common.config.services.UserServiceConfig;
import org.simpleframework.xml.Element;

public class ServicesConfig {
	
	@Element(name = "user")
	public UserServiceConfig userService;
	
}
