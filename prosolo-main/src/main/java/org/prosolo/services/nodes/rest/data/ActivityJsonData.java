package org.prosolo.services.nodes.rest.data;

import java.util.Random;

import org.prosolo.common.domainmodel.activities.TargetActivity;

import com.google.gson.annotations.SerializedName;

public class ActivityJsonData {

	private long id;
	private String name;
	@SerializedName("Complexity")
	private double complexity;
	@SerializedName("Time")
	private double timeNeeded;
	
	public ActivityJsonData() {
		
	}
	
	public ActivityJsonData(TargetActivity ta) {
		this.id = ta.getId();
		this.name = ta.getTitle();
		
		int min = 0;
		int max = 10;
		
		Random r = new Random();
		double randomValue1 = min + (max - min) * r.nextDouble();
		double randomValue2 = min + (max - min) * r.nextDouble();
		this.complexity = randomValue1;
		this.timeNeeded = randomValue2;
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
	public double getTimeNeeded() {
		return timeNeeded;
	}
	public void setTimeNeeded(double timeNeeded) {
		this.timeNeeded = timeNeeded;
	}
	
	
	
}
