package org.prosolo.web.lti;

import java.util.List;

import org.prosolo.web.lti.json.data.InlineContext;
import org.prosolo.web.lti.json.data.Service;

public class TCProfile {
	
	private String id;
	private List<InlineContext> contexts;
	private List<Service> services;
	private List<String> capabilities;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<InlineContext> getContexts() {
		return contexts;
	}
	public void setContexts(List<InlineContext> contexts) {
		this.contexts = contexts;
	}
	public List<Service> getServices() {
		return services;
	}
	public void setServices(List<Service> services) {
		this.services = services;
	}
	public List<String> getCapabilities() {
		return capabilities;
	}
	public void setCapabilities(List<String> capabilities) {
		this.capabilities = capabilities;
	}
		
	
}
