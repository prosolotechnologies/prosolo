package org.prosolo.domainmodel.user.reminders;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.user.reminders.Reminder;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Entity
public class RequestReminder extends Reminder {

	private static final long serialVersionUID = -3955801539785635457L;

	private Request requestRsource;

	@OneToOne
	public Request getRequestResource() {
		return requestRsource;
	}

	public void setRequestResource(Request resource) {
		this.requestRsource = resource;
	}

}
