package org.prosolo.common.domainmodel.activities.requests;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.reminders.EventReminder;
import org.prosolo.common.domainmodel.activities.requests.Request;

@Entity
public class JoinEventRequest  extends Request {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8167533488369883375L;
	private EventReminder eventReminder;
	
	@OneToOne
	public EventReminder getEventReminder() {
		return eventReminder;
	}

	public void setEventReminder(EventReminder eventReminder) {
		this.eventReminder = eventReminder;
	}

	@Override
	@Transient
	public BaseEntity getResource() {
		// TODO Auto-generated method stub
		return eventReminder;
	}

	@Override
	public void setResource(BaseEntity resource) {
		// TODO Auto-generated method stub
		this.eventReminder=(EventReminder) resource;
	}


}
