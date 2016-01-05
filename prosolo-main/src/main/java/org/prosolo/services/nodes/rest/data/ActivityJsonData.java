package org.prosolo.services.nodes.rest.data;

import com.google.gson.annotations.SerializedName;

public class ActivityJsonData {

	private long id;
	private String name;
	@SerializedName("Complexity")
	private double complexity;
	@SerializedName("Time")
	private int timeNeeded;
	private boolean completed;
	
	public ActivityJsonData() {
		
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getComplexity() {
		return complexity;
	}
	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}
	public int getTimeNeeded() {
		return timeNeeded;
	}
	public void setTimeNeeded(int timeNeeded) {
		this.timeNeeded = timeNeeded;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
}
