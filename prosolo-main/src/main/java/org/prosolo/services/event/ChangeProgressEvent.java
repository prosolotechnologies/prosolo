package org.prosolo.services.event;

import javax.persistence.Column;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.workflow.Scale;

//@Entity
public class ChangeProgressEvent extends Event {

	private static final long serialVersionUID = -1270293076196460170L;

	private double newProgressValue;
	private Scale progressScaleUsed;

	public ChangeProgressEvent() {
		super(EventType.ChangeProgress);
	}

	/**
	 * @return the newProgressValue
	 */
	@Column(nullable = true)
	public double getNewProgressValue() {
		return newProgressValue;
	}

	/**
	 * @param newProgressValue the newProgressValue to set
	 */
	public void setNewProgressValue(double newProgressValue) {
		this.newProgressValue = newProgressValue;
	}

	/**
	 * @return the progressScaleUsed
	 */
	@OneToOne
	public Scale getProgressScaleUsed() {
		return progressScaleUsed;
	}

	/**
	 * @param progressScaleUsed the progressScaleUsed to set
	 */
	public void setProgressScaleUsed(Scale progressScaleUsed) {
		if (null != progressScaleUsed) {
			this.progressScaleUsed = progressScaleUsed;
		}  
	}
	
}
