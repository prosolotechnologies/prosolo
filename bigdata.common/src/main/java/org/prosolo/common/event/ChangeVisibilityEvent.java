package org.prosolo.common.event;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.VisibilityType;

import javax.persistence.Column;

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
