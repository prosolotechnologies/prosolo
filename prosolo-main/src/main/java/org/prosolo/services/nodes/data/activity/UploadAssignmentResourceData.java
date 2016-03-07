package org.prosolo.services.nodes.data.activity;

public class UploadAssignmentResourceData extends ResourceData {

	private int duration;
	private boolean visibleToEveryone;
	private int maxFilesNumber;
	
	public UploadAssignmentResourceData() {
		setActivityType();
	}

	public UploadAssignmentResourceData(int duration, boolean visibleToEveryone, int maxFilesNumber) {
		this.duration = duration;
		this.visibleToEveryone = visibleToEveryone;
		this.maxFilesNumber = maxFilesNumber;
		setActivityType();
	}


	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

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

	@Override
	void setActivityType() {
		this.activityType = ActivityType.ASSIGNMENT_UPLOAD;
	}
	
}
