package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.events.analyzers.activityTimeSpent.data.ActivityTimeSpentData;
import org.prosolo.bigdata.events.pojo.LogEvent;

import com.google.gson.JsonElement;
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
			//Pattern pattern = Pattern.compile("learn.targetGoal.(\\d+).targetComp.(\\d+).targetActivity.(\\d+).*");
			//JsonObject params = event.getParameters();
			//String context = params != null ? JsonUtil.getAsString(params, "context") : null;
			
			//Matcher m = pattern.matcher(context);
	
//			if (m.matches()) {
//			    this.activityId = Long.parseLong(m.group(3));
//			    return true;
//			}
			JsonObject lContext = event.getLearningContextJson();
			if(lContext == null || lContext.get("context") == null) {
				return false;
			}
			return checkIfTargetActivityContext(lContext.get("context").getAsJsonObject());
			
		}
		return false;
	}

	/**
	 * Recursive method that checks learning context and if context with name target_activity 
	 * is found, true is returned.
	 * 
	 * NOTE: Method has side effect - if it finds target_activity context it sets {@link #activityId} value.
	 * @param ctx
	 * @return
	 */
	private boolean checkIfTargetActivityContext(JsonObject ctx) {
		if(ctx == null) {
			return false;
		}
		if("TARGET_ACTIVITY".equals(ctx.get("name").getAsString().toUpperCase())) {
			this.activityId = ctx.get("id").getAsLong();
			return true;
		}
		JsonElement subCtx = ctx.get("context");
		if(subCtx == null) {
			return false;
		}
		return checkIfTargetActivityContext(subCtx.getAsJsonObject());
	}

	//check specific conditions as a check if 
	//learning has started
	protected abstract boolean checkAdditionalConditions();

	protected abstract boolean checkIfStoppedLearning(LogEvent nextEv);
}
