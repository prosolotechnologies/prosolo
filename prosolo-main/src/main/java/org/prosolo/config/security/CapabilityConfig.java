package org.prosolo.config.security;

import java.util.List;

import org.prosolo.common.domainmodel.organization.Capability;

public class CapabilityConfig {

	private String name;
	private String description;
	private List<String> roles;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		
		CapabilityConfig other = (CapabilityConfig) obj;
		if(this.name == null || other.name == null){
			return false;
		}
		return (this.name.equals(other.name));
	}
	
	
}
