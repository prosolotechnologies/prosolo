package org.prosolo.bigdata.events.analyzers;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.session.impl.LearningEventsCalculator;

/**
 * @author Nikola Maric
 *
 */
public class LearningEventsSessionAnalyzer implements SessionAnalyzer {
	
	private static Logger logger = Logger.getLogger(LearningEventsSessionAnalyzer.class);
	
	public static class LearningEventsSessionAnalyzerHolder {
		public static final LearningEventsSessionAnalyzer INSTANCE = new LearningEventsSessionAnalyzer();
	}
	public static LearningEventsSessionAnalyzer getInstance() {
		return LearningEventsSessionAnalyzerHolder.INSTANCE;
	}

	@Override
	public void analyzeSession(SessionRecord sessionRecord) {
		logger.info(String.format("Analyzing session for user : %s started at : %s, ended at : %s", 
				sessionRecord.getUserId(),sessionRecord.getSessionStart(), sessionRecord.getSessionEnd()));
		LearningEventsCalculator calculator = new LearningEventsCalculator();
		//TODO use dao to get events and pass them to calculator to calculate hits
	}

}
