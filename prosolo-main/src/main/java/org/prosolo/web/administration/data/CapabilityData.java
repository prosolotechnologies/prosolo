package org.prosolo.web.administration.data;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.organization.Capability;
import org.prosolo.common.domainmodel.organization.Role;

public class CapabilityData {

	private long id;
	private String description;
	private List<Long> roleIds;
	
	public CapabilityData(){
		
	}
	
	public CapabilityData(Capability c) {
		roleIds = new ArrayList<>();
		this.id = c.getId();
		this.description = c.getDescription();
		if(c.getRoles() != null){
			for(Role r:c.getRoles()){
				roleIds.add(r.getId());
			}
		}
	}

	public CapabilityData(Capability capability, List<Long> roleIds) {
		this.id = capability.getId();
		this.description = capability.getDescription();
		this.roleIds = roleIds;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	
	
	

}
