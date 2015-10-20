package org.prosolo.config.security;

import java.util.List;

public class SecurityContainer {

	private List<String> roles;
	private List<CapabilityConfig> capabilities;
	
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<CapabilityConfig> getCapabilities() {
		return capabilities;
	}
	public void setCapabilities(List<CapabilityConfig> capabilities) {
		this.capabilities = capabilities;
	}
	
	
}
