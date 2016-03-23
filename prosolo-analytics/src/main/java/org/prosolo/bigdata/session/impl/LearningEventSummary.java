package org.prosolo.bigdata.session.impl;

import java.util.List;

public class LearningEventSummary {

	private  String date;
	private long value;
	private List<Milestone> milestones;
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public List<Milestone> getMilestones() {
		return milestones;
	}
	public void setMilestones(List<Milestone> milestones) {
		this.milestones = milestones;
	}
	
	public class Milestone {
		
		private String id;
		private String process;
		private String description;
		private String type = "Competences";
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getProcess() {
			return process;
		}
		public void setProcess(String process) {
			this.process = process;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		
	}
	
}
