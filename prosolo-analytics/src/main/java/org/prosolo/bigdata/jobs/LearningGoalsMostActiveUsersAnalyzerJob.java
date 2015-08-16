package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
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
		LearningGoalsMostActiveUsersAnalyzer analyzer = new LearningGoalsMostActiveUsersAnalyzer();
		final long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
		analyzer.analyzeLearningGoalsMostActiveUsersForDay(daysSinceEpoch);
		analyzer.analyzeLearningGoalsMostActiveUsersForWeek();

	}

}
