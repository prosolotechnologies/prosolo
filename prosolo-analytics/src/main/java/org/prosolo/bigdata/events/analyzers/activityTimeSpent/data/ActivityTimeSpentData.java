package org.prosolo.bigdata.events.analyzers.activityTimeSpent.data;

public class ActivityTimeSpentData {

	private long id;
	private long timeSpent;
	
	public ActivityTimeSpentData() {
		
	}
	
	public ActivityTimeSpentData(long id, long timeSpent) {
		super();
		this.id = id;
		this.timeSpent = timeSpent;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTimeSpent() {
		return timeSpent;
	}
	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}
	
	
}
