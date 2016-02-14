package org.prosolo.bigdata.session.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.prosolo.bigdata.events.pojo.LogEvent;

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
		events.stream().forEach((event) -> analyzeEvent(event));//events.get(0).setObjectType("CourseEnrollment");events.get(0).setEventType(EventType.ENROLL_COURSE);
		return eventCounters.parallelStream()
				.map(ec -> ec.getResult())
				.filter(summary -> summary.getHitCount() > 0)
				.collect(Collectors.toList());
	}
	
	private void analyzeEvent(LogEvent event) {
		eventCounters.parallelStream().forEach((counter) -> counter.processEvent(event));
	}
	
}
