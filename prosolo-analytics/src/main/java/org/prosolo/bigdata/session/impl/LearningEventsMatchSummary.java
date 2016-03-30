package org.prosolo.bigdata.session.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.prosolo.bigdata.events.pojo.LogEvent;

public class LearningEventsMatchSummary {
	

	private final String id;
	private final String description;
	private final String process;
	//TODO session can span 2 days, perhaps? do we need a list? possible optimization spot
	private List<DailyHits> result;
	private final boolean milestone;
	//used only for milestone events
	private MilestoneType type;
	private String name;
	
	public LearningEventsMatchSummary(String id, String description,
			String process, boolean milestone, MilestoneType type, String name) {
		super();
		this.id = id;
		this.description = description;
		this.process = process;
		this.milestone = milestone;
		this.type = type;
		this.name = name;
		result = new ArrayList<>();
	}
	
	public List<DailyHits> getResult() {
		return result;
	}

	public void hit(LogEvent event, final long epochDay) {
		Optional<DailyHits> dailyHit = findDailyHit(epochDay);
		if(dailyHit.isPresent()) {
			dailyHit.get().hit();
		}
		else {
			DailyHits dh = new DailyHits(epochDay);
			dh.hit();
			result.add(dh);
		}
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
	
	private Optional<DailyHits> findDailyHit(long epochDay) {
		return result.stream()
				.filter(d -> d.getEpochDay() == epochDay)
				.findFirst();
	}
	
	public class DailyHits {
		
		private long epochDay;
		private int hitCount;
		
		public DailyHits(long epochDay) {
			super();
			this.epochDay = epochDay;
			this.hitCount = 0;
		}
		
		public long getEpochDay() {
			return epochDay;
		}

		public int getHitCount() {
			return hitCount;
		}
		
		public void hit() {
			hitCount++;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (epochDay ^ (epochDay >>> 32));
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
			DailyHits other = (DailyHits) obj;
			if (epochDay != other.epochDay)
				return false;
			return true;
		}
	
	}
	
}
