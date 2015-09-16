package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

//the party using product instances, optional
public class ServiceOwner {

	private String timestamp;
	@SerializedName("service_owner_name")
	private Description serviceOwnerName;
	private Description description;
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Description getServiceOwnerName() {
		return serviceOwnerName;
	}
	public void setServiceOwnerName(Description serviceOwnerName) {
		this.serviceOwnerName = serviceOwnerName;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}
	
	
}
