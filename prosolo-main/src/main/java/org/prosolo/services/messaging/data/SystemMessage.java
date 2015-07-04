package org.prosolo.services.messaging.data;

import java.util.Map;

import org.prosolo.bigdata.common.rabbitmq.SimpleMessage;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */

public class SystemMessage extends SimpleMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8316618008585692473L;
	private ServiceType serviceType;
	private Map<String, String> parameters;

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}
}
