package org.prosolo.common.messaging.data;

import java.util.Map;

 

/**
 * @author zoran Aug 2, 2015
 */

 
public class BroadcastMessage  extends SimpleMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 541709813329585537L;
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
