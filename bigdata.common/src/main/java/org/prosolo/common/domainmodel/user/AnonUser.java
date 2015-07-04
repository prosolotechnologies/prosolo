/**
 * 
 */
package org.prosolo.common.domainmodel.user;

import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.User;

//import org.prosolo.services.twitter.ServiceType;

/**
 * @author "Nikola Milikic"
 * 
 */
//@Entity
public class AnonUser extends User {

	private static final long serialVersionUID = -3377624647304809833L;

	private String nickname;
	private ServiceType serviceType;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

}
