package org.prosolo.bigdata.events.analyzers;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.UserSessionDBManager;
import org.prosolo.bigdata.scala.spark.SparkContextLoader$;

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
		JavaSparkContext javaSparkContext = SparkContextLoader$.MODULE$.getJSC();
		logger.info(String.format("Analyzing session for user : %s started at : %s, ended at : %s", 
				sessionRecord.getUserId(),sessionRecord.getSessionStart(), sessionRecord.getSessionEnd()));
	}

}
