package org.prosolo.services.nodes.data;

public class TargetActivityData {

	private long id;
	private boolean completed;
	private BasicActivityData activity;
	
	public TargetActivityData() {
		
	}
	
	public TargetActivityData(long id, boolean completed, BasicActivityData activity) {
		this.id = id;
		this.completed = completed;
		this.activity = activity;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public BasicActivityData getActivity() {
		return activity;
	}

	public void setActivity(BasicActivityData activity) {
		this.activity = activity;
	}
	
}
