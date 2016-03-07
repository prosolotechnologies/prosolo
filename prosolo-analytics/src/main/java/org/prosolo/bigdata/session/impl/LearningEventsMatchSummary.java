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
	
	public LearningEventsMatchSummary(String id, String description, String process, boolean milestone) {
		super();
		this.id = id;
		this.description = description;
		this.process = process;
		this.milestone = milestone;
		result = new ArrayList<>();
	}
	
	public List<DailyHits> getResult() {
		return result;
	}

	public void hit(LogEvent event, final int eventYear, final int eventDayInYear) {
		Optional<DailyHits> dailyHit = findDailyHit(eventYear, eventDayInYear);
		if(dailyHit.isPresent()) {
			dailyHit.get().hit();
		}
		else {
			DailyHits dh = new DailyHits(eventYear, eventDayInYear);
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
	
	private Optional<DailyHits> findDailyHit(int year, int dayOfYear) {
		return result.stream()
				.filter(d -> d.getDayInYear().getDayOfYear() == dayOfYear 
					&& d.getDayInYear().getYear() == year)
				.findFirst();
	}
	
	public class DailyHits {
		
		private DayInYear dayInYear;
		private int hitCount;
		
		public DailyHits(int year, int dayOfYear) {
			super();
			this.dayInYear = new DayInYear(year, dayOfYear);
			this.hitCount = 0;
		}
		
		public DayInYear getDayInYear() {
			return dayInYear;
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
			result = prime * result + ((dayInYear == null) ? 0 : dayInYear.hashCode());
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
			if (dayInYear == null) {
				if (other.dayInYear != null)
					return false;
			} else if (!dayInYear.equals(other.dayInYear))
				return false;
			return true;
		}
		
	}
	
	public class DayInYear {
		
		private final int year;
		private final int dayOfYear;
		
		public DayInYear(int year, int dayOfYear) {
			super();
			this.year = year;
			this.dayOfYear = dayOfYear;
		}

		public int getYear() {
			return year;
		}

		public int getDayOfYear() {
			return dayOfYear;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dayOfYear;
			result = prime * result + year;
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
			DayInYear other = (DayInYear) obj;
			if (dayOfYear != other.dayOfYear)
				return false;
			if (year != other.year)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DayInYear [year=" + year + ", dayOfYear=" + dayOfYear + "]";
		}
	}
	
}
