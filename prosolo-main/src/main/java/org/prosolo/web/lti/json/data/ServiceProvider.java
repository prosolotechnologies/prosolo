package org.prosolo.web.lti.json.data;

import com.google.gson.annotations.SerializedName;

//responsible for hosting one or more product instances, optional
public class ServiceProvider {

	private String guid;
	private String timestamp;
	@SerializedName("service_provider_name")
	private Description serviceProviderName;
	private Description description;
	private Contact support;
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Description getServiceProviderName() {
		return serviceProviderName;
	}
	public void setServiceProviderName(Description serviceProviderName) {
		this.serviceProviderName = serviceProviderName;
	}
	public Description getDescription() {
		return description;
	}
	public void setDescription(Description description) {
		this.description = description;
	}
	public Contact getSupport() {
		return support;
	}
	public void setSupport(Contact support) {
		this.support = support;
	}
	
	
}
