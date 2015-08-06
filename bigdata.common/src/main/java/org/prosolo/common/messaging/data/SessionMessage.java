package org.prosolo.common.messaging.data;

import java.util.HashMap;
import java.util.Map;

public class SessionMessage extends SimpleMessage {

	private static final long serialVersionUID = -6545310242907984219L;

	private long receiverId;
	private ServiceType serviceType;
	private long resourceId;
	private String resourceType;
	private Map<String, String> parameters;

	public SessionMessage() {
		parameters = new HashMap<String, String>();
	}

	public long getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(long receiverId) {
		this.receiverId = receiverId;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
