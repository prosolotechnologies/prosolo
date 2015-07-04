package org.prosolo.web.activitywall.data;

import java.io.Serializable;

import org.prosolo.domainmodel.user.ServiceType;

public class PublishingServiceData implements Serializable {

	private static final long serialVersionUID = 5255164170756888490L;

	private ServiceType serviceType;
	private String name;
	private String accountName;
	private String accountLink;
	
	public PublishingServiceData() { }
	
	public PublishingServiceData(ServiceType serviceType, String accountName, String accountLink) {
		this.serviceType = serviceType;
		this.accountName = accountName;
		this.accountLink = accountLink;
	}
	
	public PublishingServiceData(String name, String accountName, String accountLink) {
		this.name = name;
		this.accountName = accountName;
		this.accountLink = accountLink;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountLink() {
		return accountLink;
	}

	public void setAccountLink(String accountLink) {
		this.accountLink = accountLink;
	}

}
