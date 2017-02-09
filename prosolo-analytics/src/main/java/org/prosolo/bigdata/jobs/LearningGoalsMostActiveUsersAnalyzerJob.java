package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.analyzers.LearningGoalsMostActiveUsers;
import org.prosolo.bigdata.spark.LearningGoalsMostActiveUsersAnalyzer;
import org.prosolo.bigdata.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Zoran Jeremic Jun 6, 2015
 *
 */

public class LearningGoalsMostActiveUsersAnalyzerJob implements Job {
	private static Logger logger = Logger
			.getLogger(LearningGoalsMostActiveUsersAnalyzerJob.class.getName());

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("executed job for learning goals most active users");
	//	LearningGoalsMostActiveUsersAnalyzer analyzer = new LearningGoalsMostActiveUsersAnalyzer();
		LearningGoalsMostActiveUsers analyzer = new LearningGoalsMostActiveUsers();
		final long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
		 analyzer.analyzeLearningGoalsMostActiveUsersForDay(daysSinceEpoch);
		analyzer.analyzeLearningGoalsMostActiveUsersForWeek();
		logger.info("LearningGoalsMostActiveUsersAnalyzerJob JOB FINISHED");

	}

}
