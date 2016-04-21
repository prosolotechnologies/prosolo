package org.prosolo.services.event;

import javax.persistence.Column;

import org.prosolo.common.domainmodel.activities.events.EventType;

//@Entity
public class ChangeProgressEvent extends Event {

	private static final long serialVersionUID = -1270293076196460170L;

	private double newProgressValue;

	public ChangeProgressEvent() {
		super(EventType.ChangeProgress);
	}

	@Column(nullable = true)
	public double getNewProgressValue() {
		return newProgressValue;
	}

	public void setNewProgressValue(double newProgressValue) {
		this.newProgressValue = newProgressValue;
	}
	
}
