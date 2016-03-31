package org.prosolo.bigdata.session.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.prosolo.bigdata.utils.DateUtil;

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
	
	public long getSummaryEpochDay(String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return DateUtil.getDaysSinceEpoch(sdf.parse(getDate()));
	}
	
	public static class Milestone {
		
		private String id;
		private String process;
		private String description;
		private MilestoneType type;
		private String name;
		
		public Milestone() {
			super();
		}
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
		
		public MilestoneType getType() {
			return type;
		}
		public void setType(MilestoneType type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
	
}
