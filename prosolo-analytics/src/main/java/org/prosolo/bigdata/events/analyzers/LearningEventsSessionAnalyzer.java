package org.prosolo.bigdata.events.analyzers;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.impl.LearningEventsDBManagerImpl;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.impl.LearningEventsCalculator;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary;
import org.prosolo.bigdata.session.impl.LearningEventsMatchSummary.DayInYear;

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
		LearningEventsCalculator calculator = new LearningEventsCalculator();
		List<LearningEventsMatchSummary> eventsSummary = calculator.calculateNumberOfLearningEvents(events);
		//get total number of hits per day
		Map<DayInYear, Integer> numberOfLearningEvents = calculator.calculateHitsPerDay(eventsSummary);
		//create list of JSON strings for every milestone
		Map<DayInYear, List<String>> milestonesPerDay = calculator.getAllMilestonesJsonFormat(eventsSummary);
		
		LearningEventsDBManagerImpl.getInstance()
			.saveLearningEventsData(sessionRecord, numberOfLearningEvents, milestonesPerDay);
	}

}
