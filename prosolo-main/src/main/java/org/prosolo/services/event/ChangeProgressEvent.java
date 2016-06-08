package org.prosolo.services.event;

import javax.persistence.Column;

import org.prosolo.common.domainmodel.activities.events.EventType;

//@Entity
public class ChangeProgressEvent extends Event {

	private static final long serialVersionUID = -1270293076196460170L;

	private int newProgressValue;

	public ChangeProgressEvent() {
		super(EventType.ChangeProgress);
	}

	@Column(nullable = true)
	public int getNewProgressValue() {
		return newProgressValue;
	}

	public void setNewProgressValue(int newProgressValue) {
		this.newProgressValue = newProgressValue;
	}
	
}
