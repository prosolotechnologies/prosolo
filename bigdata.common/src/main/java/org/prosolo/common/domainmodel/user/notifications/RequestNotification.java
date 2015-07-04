package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.user.notifications.Notification;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Entity
public class RequestNotification extends Notification {

	private static final long serialVersionUID = 970150513877263846L;
	
	private Request request;

	@OneToOne
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	@Transient
	public Request getObject() {
		return request;
	}
}
