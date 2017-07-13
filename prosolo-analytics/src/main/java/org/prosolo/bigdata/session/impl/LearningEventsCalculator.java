package org.prosolo.bigdata.session.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary.DailyHits;
import org.prosolo.common.util.date.DateEpochUtil;

import com.google.gson.JsonObject;

/**
 * @author Nikola Maric
 *
 */
public class LearningEventsCalculator {
	
	private List<LearningEventMatchCounter> eventCounters;
	
	public LearningEventsCalculator() {
		eventCounters = LearningEventsMatcherDaoImpl.getInstance().getEventMatchers()
				.stream()
				.map((matcher) ->  new LearningEventMatchCounter(matcher))
				.collect(Collectors.toList());
	}
	
	public List<LearningEventsMatchSummary> calculateNumberOfLearningEvents(List<LogEvent> events) {
		events.stream().forEach((event) -> analyzeEvent(event));
		return eventCounters.parallelStream()
				.map(ec -> ec.getResult())
				.filter(summary -> summary.getResult().size() > 0)
				.collect(Collectors.toList());
	}
	
	public Map<Long, Integer> calculateHitsPerDay(List<LearningEventsMatchSummary> summaries){
		return summaries.stream()
	        	.flatMap(d -> d.getResult().stream())
	        	.collect(Collectors.groupingBy(dailyHits -> dailyHits.getEpochDay(),
	                    Collectors.summingInt(item -> item.getHitCount())));
	}
	
	public Map<Long, List<String>> getAllMilestonesJsonFormat(List<LearningEventsMatchSummary> summaries) {
		Map<Long, List<String>> milestoneHits = new HashMap<>();
		for(LearningEventsMatchSummary summary : summaries) {
			//go through all summaries that are milestones and have results
			if(summary.isMilestone() && summary.getResult().size() > 0) {
				//for every daily hit...
				for(DailyHits dh : summary.getResult()) {
					//create JSON String representation of milestone object
					JsonObject object = new JsonObject();
					object.addProperty("id", summary.getId());
					object.addProperty("process", summary.getProcess());
					object.addProperty("description", summary.getDescription());
					object.addProperty("type", summary.getType().getValue());
					object.addProperty("name", summary.getName());
					String milestoneString = object.toString();
					//if we do not have hit for this day of this year, create one
					if(!milestoneHits.containsKey(dh.getEpochDay())) {
						milestoneHits.put(dh.getEpochDay(), new ArrayList<String>());
					}
					//now, add milestone string for every hit 
					for(int i = 0; i < dh.getHitCount(); i++) {
						milestoneHits.get(dh.getEpochDay()).add(milestoneString);
					}
					
				}
			}
		}
		return milestoneHits;
	}
	
	private void analyzeEvent(LogEvent event) {
		/*Create date here, so every matcher can use it's values (rather than creating 
		 * date in every matcher, only to access same fields with same values) */
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(event.getTimestamp());
		eventCounters.stream().forEach((counter) -> counter.processEvent(event, DateEpochUtil.getDaysSinceEpoch(event.getTimestamp())));
	}
	
}
