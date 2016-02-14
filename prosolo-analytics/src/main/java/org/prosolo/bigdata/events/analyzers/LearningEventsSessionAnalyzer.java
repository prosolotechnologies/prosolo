package org.prosolo.bigdata.events.analyzers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.impl.LearningEventsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.impl.LearningEventsCalculator;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonObject;

/**
 * @author Nikola Maric
 *
 */
public class LearningEventsSessionAnalyzer implements SessionAnalyzer<LogEvent> {
	
	private static Logger logger = Logger.getLogger(LearningEventsSessionAnalyzer.class);
	
	public static class LearningEventsSessionAnalyzerHolder {
		public static final LearningEventsSessionAnalyzer INSTANCE = new LearningEventsSessionAnalyzer();
	}
	public static LearningEventsSessionAnalyzer getInstance() {
		return LearningEventsSessionAnalyzerHolder.INSTANCE;
	}

	@Override
	public void analyzeSession(List<LogEvent> events, SessionRecord sessionRecord) {
		Stopwatch sw = new Stopwatch().start();
		LearningEventsCalculator calculator = new LearningEventsCalculator();
		List<LearningEventsMatchSummary> eventsSummary = calculator.calculateNumberOfLearningEvents(events);
		logger.info("Calculating learning events lasted for "+sw.elapsed(TimeUnit.MILLISECONDS)+" msec");
		//get total number of hits 
		int numberOfLearningEvents = eventsSummary.stream()
				.collect(Collectors.summingInt(LearningEventsMatchSummary::getHitCount));
		//create list of JSON strings for every milestone
		List<String> milestonesJsonStrings = eventsSummary.stream()
				.filter(es -> es.isMilestone())
				.map(es -> {
					JsonObject object = new JsonObject();
					object.addProperty("id", es.getId());
					object.addProperty("process", es.getProcess());
					object.addProperty("description", es.getDescription());
					return object.toString();
				}).collect(Collectors.toList());
		
		LearningEventsDBManagerImpl.getInstance()
			.saveLearningEventsData(sessionRecord, numberOfLearningEvents, milestonesJsonStrings);
	}

}
