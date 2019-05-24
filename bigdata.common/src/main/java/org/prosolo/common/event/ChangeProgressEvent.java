package org.prosolo.common.event;

import org.prosolo.common.domainmodel.events.EventType;

import javax.persistence.Column;

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
