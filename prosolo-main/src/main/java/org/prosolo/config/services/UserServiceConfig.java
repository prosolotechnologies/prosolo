package org.prosolo.config.services;

import org.simpleframework.xml.Element;

public class UserServiceConfig {
	
	@Element(name = "default-avatar-path")
	public String defaultAvatarPath;
	
	@Element(name = "user-avatar-path")
	public String userAvatarPath;
	
	@Element(name = "default-avatar-name")
	public String defaultAvatarName;

}
