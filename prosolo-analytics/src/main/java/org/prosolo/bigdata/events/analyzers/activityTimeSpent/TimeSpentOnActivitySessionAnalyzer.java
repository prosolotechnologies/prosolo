package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.persistence.impl.ActivityDAOImpl;
import org.prosolo.bigdata.events.analyzers.SessionAnalyzer;
import org.prosolo.bigdata.events.analyzers.activityTimeSpent.data.ActivityTimeSpentData;
import org.prosolo.bigdata.events.pojo.LogEvent;

public class TimeSpentOnActivitySessionAnalyzer implements SessionAnalyzer<LogEvent> {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TimeSpentOnActivitySessionAnalyzer.class);
	
	public static class TimeSpentOnActivitySessionAnalyzerHolder {
		public static final TimeSpentOnActivitySessionAnalyzer INSTANCE = new TimeSpentOnActivitySessionAnalyzer();
	}
	
	public static TimeSpentOnActivitySessionAnalyzer getInstance() {
		return TimeSpentOnActivitySessionAnalyzerHolder.INSTANCE;
	}

	@Override
	public void analyzeSession(List<LogEvent> events, SessionRecord sessionRecord) {
		Map<Long, Long> activitiesWithTimeSpent = new HashMap<>();
		int counter = 0;
		for(LogEvent ev : events) {
			Optional<TimeSpentOnActivityProcessor> opt = TimeSpentOnActivityProcessorFactory
					.getTimeSpentOnActivityProcessor(ev);
			if(opt.isPresent()) {
				TimeSpentOnActivityProcessor processor = opt.get();
				Optional<ActivityTimeSpentData> optData = processor
						.getTimeSpent(counter, events, sessionRecord);
				if(optData.isPresent()) {
					ActivityTimeSpentData data = optData.get();
					long id = data.getId();
					activitiesWithTimeSpent.put(id, 
							getNewTimeSpentOnActivity(
									activitiesWithTimeSpent.get(id), data.getTimeSpent()));
				}
			}
			counter++;
		}
		
		ActivityDAOImpl.getInstance().updateTimeSpentOnActivities(activitiesWithTimeSpent);
	}

	//if currentTimeSpent is null we return timeSpent as a result, otherwise 
	//we need to add two numbers and return result
	private long getNewTimeSpentOnActivity(Long currentTimeSpent, long timeSpent) {
		if(currentTimeSpent != null) {
			return currentTimeSpent + timeSpent;
		}
		return timeSpent;
	}

}
