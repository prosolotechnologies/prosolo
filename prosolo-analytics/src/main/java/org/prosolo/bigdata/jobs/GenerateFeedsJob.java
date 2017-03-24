package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.feeds.DigestManager1$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GenerateFeedsJob  implements Job {
	private static Logger logger = Logger
			.getLogger(GenerateFeedsJob.class.getName());
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
logger.info("STARTING GENERATION OF FEED DIGESTS EXECUTED JOB");
		
		//digestManager.createFeedDiggestsAndSendEmails();
		DigestManager1$ digestManager = DigestManager1$.MODULE$;
		digestManager.createFeedDigestsAndSendEmails();
		logger.info("COMPLETED GENERATION OF FEED DIGESTS");
		
	}

}
