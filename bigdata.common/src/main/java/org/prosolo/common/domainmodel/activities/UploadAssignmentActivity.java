package org.prosolo.common.domainmodel.activities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activities.Activity;

/**
 * @author Zoran Jeremic Nov 17, 2013
 */
@Entity
public class UploadAssignmentActivity extends Activity {

	private static final long serialVersionUID = 2424125805116688990L;

	private int duration;
	private boolean visibleToEveryone;
	private int maxFilesNumber;

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isVisibleToEveryone() {
		return visibleToEveryone;
	}

	public void setVisibleToEveryone(boolean visibleToEveryone) {
		this.visibleToEveryone = visibleToEveryone;
	}

	public int getMaxFilesNumber() {
		return maxFilesNumber;
	}

	public void setMaxFilesNumber(int maxFilesNumber) {
		this.maxFilesNumber = maxFilesNumber;
	}

}
