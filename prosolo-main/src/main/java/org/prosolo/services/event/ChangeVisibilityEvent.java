package org.prosolo.services.event;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.organization.VisibilityType;

//@Entity
public class ChangeVisibilityEvent extends Event {

	private static final long serialVersionUID = -8343762971583142544L;

	private VisibilityType newVisibility;

	public ChangeVisibilityEvent() {
		super(EventType.ChangeVisibility);
	}
	
	/**
	 * @return the newVisibility
	 */
	@Column(nullable = true)
	public VisibilityType getNewVisibility() {
		return newVisibility;
	}

	/**
	 * @param newVisibility the newVisibility to set
	 */
	public void setNewVisibility(VisibilityType newVisibility) {
		this.newVisibility = newVisibility;
	}

}
