package org.prosolo.bigdata.session.impl;

public class LearningEventsMatchSummary {
	

	private final String id;
	private final String description;
	private final String process;
	private int hitCount = 0;
	private final boolean milestone;
	
	public LearningEventsMatchSummary(String id, String description, String process, boolean milestone) {
		super();
		this.id = id;
		this.description = description;
		this.process = process;
		this.milestone = milestone;
	}
	
	public int getHitCount() {
		return hitCount;
	}
	public void hit() {
		hitCount++;
	}
	
	public boolean isMilestone() {
		return milestone;
	}
	
	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getProcess() {
		return process;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (milestone ? 1231 : 1237);
		result = prime * result + ((process == null) ? 0 : process.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LearningEventsMatchSummary other = (LearningEventsMatchSummary) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (milestone != other.milestone)
			return false;
		if (process == null) {
			if (other.process != null)
				return false;
		} else if (!process.equals(other.process))
			return false;
		return true;
	}

	
}
