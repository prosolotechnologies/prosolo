package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.events.analyzers.activityTimeSpent.data.ActivityTimeSpentData;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.utils.JsonUtil;

import com.google.gson.JsonObject;

public abstract class TimeSpentOnActivityProcessor {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TimeSpentOnActivityProcessor.class);

	protected LogEvent event;
	protected long activityId;
	
	public TimeSpentOnActivityProcessor(LogEvent event) {
		this.event = event;
	}
	
	public Optional<ActivityTimeSpentData> getTimeSpent(int eventIndex, List<LogEvent> events,
			SessionRecord sessionEndRecord) {
		if(checkIfStartedLearning()) {
			LogEvent stopEvent = null;
			int size = events.size();
			for(int i = eventIndex + 1; i < size; i++) {
				LogEvent nextEv = events.get(i);
				boolean stopped = checkIfStoppedLearning(nextEv);
				if(stopped) {
					stopEvent = nextEv;
					break;
				}
			}
			
			//if there is not event that means learning is stopped, use session end timestamp
			//as learning stop time
			long stoppedLearningTimestamp = stopEvent == null ? sessionEndRecord.getSessionEnd() : 
				stopEvent.getTimestamp();
			
			long timeSpent = stoppedLearningTimestamp - event.getTimestamp();
			
			return Optional.of(new ActivityTimeSpentData(activityId, timeSpent));
			
		} 
		
		return Optional.empty();
	}

	private boolean checkIfStartedLearning() {
		if(checkAdditionalConditions()) {
			Pattern pattern = Pattern.compile("learn.targetGoal.(\\d+).targetComp.(\\d+).targetActivity.(\\d+).*");
			
			JsonObject params = event.getParameters();
			String context = params != null ? JsonUtil.getAsString(params, "context") : null;
			
			Matcher m = pattern.matcher(context);
	
			if (m.matches()) {
			    this.activityId = Long.parseLong(m.group(3));
			    return true;
			}
		}
		return false;
	}

	//check specific conditions as a check if 
	//learning has started
	protected abstract boolean checkAdditionalConditions();

	protected abstract boolean checkIfStoppedLearning(LogEvent nextEv);
}
